package org.easysql.query.truncate

import org.easysql.ast.expr.SqlIdentifierExpr
import org.easysql.ast.statement.truncate.SqlTruncate
import org.easysql.database.DB
import org.easysql.dsl.TableSchema
import org.easysql.query.ReviseQuery
import org.easysql.util.toSqlString

import java.sql.Connection

class Truncate extends ReviseQuery {
    private val sqlTruncate = SqlTruncate()

    infix def truncate(table: TableSchema | String): Truncate = {
        val tableName = table match {
            case i: TableSchema => i.tableName
            case s: String => s
        }
        this.sqlTruncate.table = Some(SqlIdentifierExpr(tableName))

        this
    }

    override def sql(db: DB): String = toSqlString(sqlTruncate, db)

    override def toSql(using db: DB): String = toSqlString(sqlTruncate, db)
}

object Truncate {
    def apply(): Truncate = new Truncate()
}
