package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.jdbc.*
import org.easysql.dsl.*
import org.easysql.bind.*
import org.easysql.ast.SqlDataType

import java.sql.Connection

class JdbcTransaction(db: DB, conn: Connection) extends DBTransaction(db) {
    override inline def run(query: ReviseQuery): Int = jdbcExec(conn, query.sql(db))

    override inline def runAndReturnKey(query: Insert[_, _]): List[Long] = 
        jdbcExecReturnKey(conn, query.sql(db))

    override inline def queryToList(sql: String): List[Map[String, Any]] = 
        jdbcQuery(conn, sql)

    override inline def queryToList[T <: Tuple](query: SelectQuery[T]): List[EliminateTuple1[T]] =
        jdbcQueryToArray(conn, query.sql(db)).map(i => bindSelect[EliminateTuple1[T]].apply(i))

    override inline def queryToList[T](query: Query[T]): List[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = 
        jdbcQueryToArray(conn, query.sql(db)).map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))

    override inline def find[T <: Tuple](query: SelectQuery[T]): Option[EliminateTuple1[T]] = 
        jdbcQueryToArray(conn, query.sql(db)).headOption.map(i => bindSelect[EliminateTuple1[T]].apply(i))

    override inline def find[T](query: Query[T]): Option[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = 
        jdbcQueryToArray(conn, query.sql(db)).headOption.map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))

    override inline def page[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean): Page[EliminateTuple1[T]] = {
        val data = if (pageSize == 0) {
            List[EliminateTuple1[T]]()
        } else {
            jdbcQueryToArray(conn, query.pageSql(pageSize, pageNum)(db)).map(i => bindSelect[EliminateTuple1[T]].apply(i))
        }

        val count = if (needCount) {
            fetchCount(query)
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

        new Page[EliminateTuple1[T]](totalPage, count, data)
    }

    override inline def fetchCount(query: Select[?]): Long = jdbcQueryCount(conn, query.countSql(db))
}
