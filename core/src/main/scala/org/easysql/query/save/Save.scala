package org.easysql.query.save

import org.easysql.ast.expr.SqlIdentifierExpr
import org.easysql.database.{DB, TableEntity}
import org.easysql.query.ReviseQuery
import org.easysql.ast.statement.upsert.SqlUpsert
import org.easysql.dsl.*
import org.easysql.util.*
import org.easysql.visitor.*

import java.sql.Connection

class Save extends ReviseQuery {
    val sqlUpsert: SqlUpsert = SqlUpsert()

//    def save[T <: TableEntity[_]](entity: T)(using t: TableSchema[T]): Save = {
//        sqlUpsert.table = Some(SqlIdentifierExpr(t.tableName))
//        
//        t.$pkCols.foreach { pk =>
//            sqlUpsert.primaryColumns.addOne(SqlIdentifierExpr(pk.column))
//            sqlUpsert.columns.addOne(SqlIdentifierExpr(pk.column))
//            sqlUpsert.value.addOne(visitExpr(anyToExpr(pk.bind.get.apply(entity))))
//        }
//        
//        t.$columns.foreach { col =>
//            val (column, value) = col match {
//                case TableColumnExpr(_, name, _, bind) => SqlIdentifierExpr(name) -> anyToExpr(bind.get.apply(entity))
//                case NullableColumnExpr(_, name, _, bind) => SqlIdentifierExpr(name) -> anyToExpr(bind.get.apply(entity))
//            }
//            
//            sqlUpsert.columns.addOne(column)
//            sqlUpsert.updateColumns.addOne(column)
//            sqlUpsert.value.addOne(visitExpr(value))
//        }
//        
//        this
//    }

    override def sql(db: DB): String = toSqlString(sqlUpsert, db)

    override def toSql(using db: DB): String = toSqlString(sqlUpsert, db)
}

object Save {
    def apply(): Save = new Save()
}
