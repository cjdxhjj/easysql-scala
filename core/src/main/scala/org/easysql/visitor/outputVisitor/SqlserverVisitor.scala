package org.easysql.visitor.outputVisitor

import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.select.SqlSelect
import org.easysql.ast.statement.upsert.SqlUpsert

import java.sql.SQLException

class SqlserverVisitor extends SqlVisitor {
    override def visitSqlLimit(sqlLimit: SqlLimit): Unit = {
        sqlBuilder.append(s"OFFSET ${sqlLimit.offset} ROWS FETCH NEXT ${sqlLimit.limit} ROWS ONLY")
    }

    override def visitSqlForUpdate(): Unit = {
        sqlBuilder.append("WITH (UPDLOCK)")
    }

    override def visitSqlSelect(select: SqlSelect): Unit = {
        sqlBuilder.append("SELECT ")

        if (select.selectList.isEmpty) {
            throw SQLException("SELECT列表为空")
        }

        if (select.distinct) {
            sqlBuilder.append("DISTINCT ")
        }

        printList(select.selectList.toList, visitSqlSelectItem)

        select.from.foreach { it =>
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            sqlBuilder.append("FROM ")
            visitSqlTableSource(it)
        }

        if (select.forUpdate) {
            sqlBuilder.append(" ")
            visitSqlForUpdate()
        }

        select.where.foreach { it =>
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            sqlBuilder.append("WHERE ")
            visitSqlExpr(it)
        }

        if (select.groupBy.nonEmpty) {
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            sqlBuilder.append("GROUP BY ")
            printList(select.groupBy.toList, visitSqlExpr)
        }

        select.having.foreach { it =>
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            sqlBuilder.append("HAVING ")
            visitSqlExpr(it)
        }

        if (select.orderBy.nonEmpty) {
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            sqlBuilder.append("ORDER BY ")
            printList(select.orderBy.toList, visitSqlOrderBy)
        }

        select.limit.foreach { it =>
            sqlBuilder.append("\n")
            printSpace(spaceNum)
            visitSqlLimit(it)
        }
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
        sqlBuilder.append(s") ${quote}t2${quote}")

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
