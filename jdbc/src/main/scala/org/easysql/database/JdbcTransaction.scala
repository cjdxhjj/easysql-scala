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

class JdbcTransaction(db: DB, conn: Connection) extends DBTransaction(db) {
    override inline def run(query: ReviseQuery)(using logger: Logger): Int = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcExec(conn, sql)
    }

    override inline def runAndReturnKey(query: Insert[_, _])(using logger: Logger): List[Long] = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcExecReturnKey(conn, sql)
    }

    override inline def query(sql: String)(using logger: Logger): List[Map[String, Any]] = {
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQuery(conn, sql)
    }

    override inline def query[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): List[ResultType[T]] = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).map(i => bindSelect[ResultType[T]].apply(i))
    }

    override inline def query[T](query: Query[T])(using logger: Logger): List[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))
    }

    override inline def find[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): Option[ResultType[T]] = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).headOption.map(i => bindSelect[ResultType[T]].apply(i))
    }

    override inline def find[T](query: Query[T])(using logger: Logger): Option[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).headOption.map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))
    }

    override inline def page[T <: Tuple](query: Select[T, _])(pageSize: Int, pageNum: Int, needCount: Boolean)(using logger: Logger): Page[ResultType[T]] = {
        val data = if (pageSize == 0) {
            List[ResultType[T]]()
        } else {
            val sql = query.pageSql(pageSize, pageNum)(db)
            logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
            jdbcQueryToArray(conn, sql).map(i => bindSelect[ResultType[T]].apply(i))
        }

        val count = if (needCount) {
            fetchCount(query)(using logger)
        } else {
            0l
        }

        val totalPage = if (count == 0 || pageSize == 0) {
            0
        } else {
            if (count % pageSize == 0) {
                count / pageSize
            } else {
                count / pageSize + 1
            }
        }

        new Page[ResultType[T]](totalPage, count, data)
    }

    override inline def fetchCount(query: Select[_, _])(using logger: Logger): Long = {
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryCount(conn, sql)
    }
}
