package org.easysql.query.delete

import org.easysql.ast.SqlDataType
import org.easysql.ast.statement.delete.SqlDelete
import org.easysql.ast.table.SqlIdentTable
import org.easysql.database.DB
import org.easysql.dsl.*
import org.easysql.query.ReviseQuery
import org.easysql.util.toSqlString
import org.easysql.visitor.*
import org.easysql.macros.*

import java.sql.{Connection, SQLException}
import java.util.Date

class Delete extends ReviseQuery {
    private val sqlDelete = SqlDelete()

    infix def deleteFrom(table: TableSchema[_]): Delete = {
        this.sqlDelete.table = Some(SqlIdentTable(table._tableName))
        this
    }

    inline def delete[T <: Product](pk: SqlDataType | Tuple): Delete = {
        val (tableName, cols) = pkMacro[T, pk.type]
        sqlDelete.table = Some(SqlIdentTable(tableName))

        inline pk match {
            case t: Tuple => t.toArray.zip(cols).foreach { (p, c) =>
                sqlDelete.addCondition(visitExpr(ColumnExpr(c).equal(p)))
            }
            case _ => sqlDelete.addCondition(visitExpr(ColumnExpr(cols.head).equal(pk)))
        }

        this
    }

    infix def where(condition: Expr[_]): Delete = {
        sqlDelete.addCondition(getExpr(condition))
        this
    }

    def where(test: () => Boolean, condition: Expr[_]): Delete = {
        if (test()) {
            sqlDelete.addCondition(getExpr(condition))
        }
        this
    }

    def where(test: Boolean, condition: Expr[_]): Delete = {
        if (test) {
            sqlDelete.addCondition(getExpr(condition))
        }
        this
    }

    override def sql(db: DB): String = toSqlString(sqlDelete, db)

    override def toSql(using db: DB): String = toSqlString(sqlDelete, db)
}

object Delete {
    def apply(): Delete = new Delete()
}
