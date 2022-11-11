package org.easysql.query.save

import org.easysql.ast.expr.SqlIdentExpr
import org.easysql.database.DB
import org.easysql.ast.table.SqlIdentTable
import org.easysql.query.ReviseQuery
import org.easysql.ast.statement.upsert.SqlUpsert
import org.easysql.dsl.*
import org.easysql.util.*
import org.easysql.visitor.*
import org.easysql.macros.*

import java.sql.Connection

class Save extends ReviseQuery {
    val sqlUpsert: SqlUpsert = SqlUpsert()

    inline def save[T <: Product](entity: T): Save = {
        val (tableName, pkList, colList) = updateMacro[T]

        sqlUpsert.table = Some(SqlIdentTable(tableName))

        pkList.foreach { pk =>
            sqlUpsert.primaryColumns.addOne(SqlIdentExpr(pk._1))
            sqlUpsert.columns.addOne(SqlIdentExpr(pk._1))
            sqlUpsert.value.addOne(visitExpr(anyToExpr(pk._2.apply(entity))))
        }

        colList.foreach { col =>
            sqlUpsert.columns.addOne(SqlIdentExpr(col._1))
            sqlUpsert.updateColumns.addOne(SqlIdentExpr(col._1))
            sqlUpsert.value.addOne(visitExpr(anyToExpr(col._2.apply(entity))))
        }

        this
    }
    
    override def sql(db: DB): String = toSqlString(sqlUpsert, db)

    override def toSql(using db: DB): String = toSqlString(sqlUpsert, db)
}

object Save {
    def apply(): Save = new Save()
}
