package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.dsl.*
import org.easysql.ast.SqlDataType

trait DBOperater {
    inline def run(query: ReviseQuery): Int

    inline def runAndReturnKey(query: Insert[_, _]): List[Long]

    inline def queryToList(sql: String): List[Map[String, Any]]

    inline def queryToList[T <: Tuple](query: SelectQuery[T, _]): List[EliminateTuple1[T]]

    inline def queryToList[T](query: Query[T]): List[FlatType[FlatType[T, SqlDataType, Expr],Product,TableSchema]]

    inline def find[T <: Tuple](query: SelectQuery[T, _]): Option[EliminateTuple1[T]]

    inline def find[T](query: Query[T]): Option[FlatType[FlatType[T, SqlDataType, Expr],Product,TableSchema]]

    inline def page[T <: Tuple](query: Select[T, _])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[EliminateTuple1[T]]

    inline def fetchCount(query: Select[_, _]): Long
}