package org.easysql.visitor.outputVisitor

import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.upsert.SqlUpsert

class OracleVisitor extends SqlVisitor {
    override def visitSqlLimit(sqlLimit: SqlLimit): Unit = {
        sqlBuilder.append(s"OFFSET ${sqlLimit.offset} ROWS FETCH FIRST ${sqlLimit.limit} ROWS ONLY")
    }

    override def printWithRecursive(): Unit = {}

    override def visitSqlUpsert(sqlUpsert: SqlUpsert): Unit = {
        sqlBuilder.append("MERGE INTO ")
        visitSqlExpr(sqlUpsert.table.get)
        sqlBuilder.append(s" ${quote}t1${quote}")

        sqlBuilder.append(" USING (")
        sqlBuilder.append("SELECT ")
        for (index <- sqlUpsert.columns.indices) {
            visitSqlExpr(sqlUpsert.value(index))
            sqlBuilder.append(" AS ")
            visitSqlExpr(sqlUpsert.columns(index))
            if (index < sqlUpsert.columns.size - 1) {
                sqlBuilder.append(",")
                sqlBuilder.append(" ")
            }
        }
        sqlBuilder.append(s" FROM ${quote}dual${quote}) ${quote}t2${quote}")

        sqlBuilder.append("\nON (")
        for (index <- sqlUpsert.primaryColumns.indices) {
            sqlBuilder.append(s"${quote}t1${quote}.")
            visitSqlExpr(sqlUpsert.primaryColumns(index))
            sqlBuilder.append(" = ")
            sqlBuilder.append(s"${quote}t2${quote}.")
            visitSqlExpr(sqlUpsert.primaryColumns(index))
            if (index < sqlUpsert.primaryColumns.size - 1) {
                sqlBuilder.append(" AND ")
            }
        }
        sqlBuilder.append(")")

        sqlBuilder.append("\nWHEN MATCHED THEN UPDATE SET ")
        printList(sqlUpsert.updateColumns.toList, { it =>
            sqlBuilder.append(s"${quote}t1${quote}.")
            visitSqlExpr(it)
            sqlBuilder.append(" = ")
            sqlBuilder.append(s"${quote}t2${quote}.")
            visitSqlExpr(it)
        })

        sqlBuilder.append("\nWHEN NOT MATCHED THEN INSERT")
        sqlBuilder.append(" (")
        printList(sqlUpsert.columns.toList, { it =>
            sqlBuilder.append(s"${quote}t1${quote}.")
            visitSqlExpr(it)
        })
        sqlBuilder.append(")")

        sqlBuilder.append(" VALUES")
        sqlBuilder.append(" (")
        printList(sqlUpsert.value.toList, visitSqlExpr)
        sqlBuilder.append(")")
    }
}
