package org.easysql.visitor.outputVisitor

import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.upsert.SqlUpsert

class SqliteVisitor extends SqlVisitor {
    override def visitSqlLimit(sqlLimit: SqlLimit): Unit = {
        sqlBuilder.append(s"LIMIT ${sqlLimit.offset}, ${sqlLimit.limit}")
    }

    override def visitSqlUpsert(sqlUpsert: SqlUpsert): Unit = {
        sqlBuilder.append("INSERT OR REPLACE INTO ")
        visitSqlExpr(sqlUpsert.table.get)

        sqlBuilder.append(" (")
        printList(sqlUpsert.columns.toList, visitSqlExpr)
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES")
        sqlBuilder.append(" (")
        printList(sqlUpsert.value.toList, visitSqlExpr)
        sqlBuilder.append(")")
    }
}
