package org.easysql.visitor.outputVisitor

import org.easysql.ast.expr.{SqlExpr, SqlListExpr}
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.select.SqlValuesSelect
import org.easysql.ast.statement.upsert.SqlUpsert

class MysqlVisitor extends SqlVisitor {
    override val quote = "`"

    override def visitSqlLimit(sqlLimit: SqlLimit): Unit = {
        sqlBuilder.append(s"LIMIT ${sqlLimit.offset}, ${sqlLimit.limit}")
    }

    override def visitSqlUpsert(sqlUpsert: SqlUpsert): Unit = {
        sqlBuilder.append("INSERT INTO ")
        visitSqlExpr(sqlUpsert.table.get)

        sqlBuilder.append(" (")
        printList(sqlUpsert.columns.toList, visitSqlExpr)
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES")
        sqlBuilder.append(" (")
        printList(sqlUpsert.value.toList, visitSqlExpr)
        sqlBuilder.append(")")

        sqlBuilder.append(" ON DUPLICATE KEY UPDATE ")

        printList(sqlUpsert.updateColumns.toList, { it =>
            visitSqlExpr(it)
            sqlBuilder.append(" = VALUES(")
            visitSqlExpr(it)
            sqlBuilder.append(")")
        })
    }

    override def visitSqlValuesSelect(sqlValuesSelect: SqlValuesSelect): Unit = {
        sqlBuilder.append("VALUES ")
        printList(sqlValuesSelect.values.toList.map(it => SqlListExpr(it)), { it =>
            sqlBuilder.append("ROW")
            visitSqlExpr(it)
        })
    }
}
