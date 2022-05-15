package org.easysql.visitor.outputVisitor

import org.easysql.ast.statement.upsert.SqlUpsert

class PgsqlVisitor extends SqlVisitor {
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

        sqlBuilder.append(" DO UPDATE SET ")

        printList(sqlUpsert.updateColumns.toList, { it =>
            visitSqlExpr(it)
            sqlBuilder.append(" = ")
            sqlBuilder.append("EXCLUDED.")
            visitSqlExpr(it)
        })
    }
}
