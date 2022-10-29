package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.dsl.*
import org.easysql.ast.SqlDataType

trait DBOperater {
    inline def run(query: ReviseQuery)(using logger: Logger): Int

    inline def runAndReturnKey(query: Insert[_, _])(using logger: Logger): List[Long]

    inline def query(sql: String)(using logger: Logger): List[Map[String, Any]]

    inline def query[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): List[ResultType[T]]

    inline def query[T](query: Query[T])(using logger: Logger): List[FlatType[FlatType[T, SqlDataType, Expr],Product,TableSchema]]

    inline def find[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): Option[ResultType[T]]

    inline def find[T](query: Query[T])(using logger: Logger): Option[FlatType[FlatType[T, SqlDataType, Expr],Product,TableSchema]]

    inline def page[T <: Tuple](query: Select[T, _])(pageSize: Int, pageNum: Int, needCount: Boolean)(using logger: Logger): Page[ResultType[T]]

    inline def fetchCount(query: Select[_, _])(using logger: Logger): Long
}

type Logger = { def info(text: String): Unit }