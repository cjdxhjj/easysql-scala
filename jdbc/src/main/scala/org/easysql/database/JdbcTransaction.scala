package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.jdbc.*
import org.easysql.dsl.*
import org.easysql.bind.*
import org.easysql.ast.SqlDataType

import java.sql.Connection
import reflect.Selectable.reflectiveSelectable

class JdbcTransaction(override val db: DB, conn: Connection) extends DBTransaction(db) {
    private[database] override def runSql(sql: String): Int = jdbcExec(conn, sql)

    private[database] override def runSqlAndReturnKey(sql: String): List[Long] = jdbcExecReturnKey(conn, sql)

    private[database] override def querySql(sql: String): List[Array[Any]] = jdbcQueryToArray(conn, sql)

    private[database] override def querySqlToMap(sql: String): List[Map[String, Any]] = jdbcQuery(conn, sql)

    private[database] override def querySqlCount(sql: String): Long = jdbcQueryCount(conn, sql)
}
