package org.easysql.query.delete

import org.easysql.ast.SqlDataType
import org.easysql.ast.expr.SqlIdentifierExpr
import org.easysql.ast.statement.delete.SqlDelete
import org.easysql.database.{DB, TableEntity}
import org.easysql.dsl.{Expr, TableColumnExpr, TableSchema, PK}
import org.easysql.query.ReviseQuery
import org.easysql.util.toSqlString
import org.easysql.visitor.getExpr

import java.sql.{Connection, SQLException}

class Delete extends ReviseQuery {
    private val sqlDelete = SqlDelete()

    infix def deleteFrom(table: TableSchema[_]): Delete = {
        this.sqlDelete.table = Some(SqlIdentifierExpr(table.tableName))
        this
    }

//    def delete[T <: TableEntity[_]](pk: PK[T])(using t: TableSchema[T]): Delete = {
//        sqlDelete.table = Some(SqlIdentifierExpr(t.tableName))
//
//        pk match {
//            case tuple: Tuple =>
//                t.$pkCols.zip(tuple.toArray).foreach { pkCol =>
//                    sqlDelete.addCondition(getExpr(pkCol._1.equal(pkCol._2)))
//                }
//            case _ => sqlDelete.addCondition(getExpr(t.$pkCols.head.equal(pk)))
//        }
//
//        this
//    }

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
