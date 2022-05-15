package org.easysql.query.save

import org.easysql.database.{DB, TableEntity}
import org.easysql.query.ReviseQuery
import org.easysql.ast.statement.upsert.SqlUpsert
import org.easysql.util.toSqlString
import org.easysql.macros.saveMacro

import java.sql.Connection

class Save extends ReviseQuery {
    val sqlUpsert: SqlUpsert = SqlUpsert()

    inline def save[T <: TableEntity[_]](entity: T): Save = {
        saveMacro[T](this, entity)
        
        this
    }

    override def sql(db: DB): String = toSqlString(sqlUpsert, db)

    override def toSql(using db: DB): String = toSqlString(sqlUpsert, db)
}

object Save {
    def apply(): Save = new Save()
}
