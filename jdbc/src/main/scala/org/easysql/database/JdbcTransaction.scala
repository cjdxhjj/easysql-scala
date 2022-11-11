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

inline def run(query: ReviseQuery)(using logger: Logger, t: JdbcTransaction): Int = t.run(query)

inline def runAndReturnKey(query: Insert[_, _])(using logger: Logger, t: JdbcTransaction): List[Long] = t.runAndReturnKey(query)

inline def query(sql: String)(using logger: Logger, t: JdbcTransaction): List[Map[String, Any]] = t.query(sql)

inline def query[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger, t: JdbcTransaction): List[ResultType[T]] = t.query(query)

inline def find[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger, t: JdbcTransaction): Option[ResultType[T]] = t.find(query)

inline def page[T <: Tuple](query: SelectQuery[T, _])(pageSize: Int, pageNum: Int, queryCount: Boolean)(using logger: Logger, t: JdbcTransaction): Page[ResultType[T]] = t.page(query)(pageSize, pageNum, queryCount)

inline def fetchCount(query: SelectQuery[_, _])(using logger: Logger, t: JdbcTransaction): Long = t.fetchCount(query)