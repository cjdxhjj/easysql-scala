package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.dsl.*
import org.easysql.ast.SqlDataType

trait DBOperater {
    inline def run(query: ReviseQuery): Int

    inline def runAndReturnKey(query: Insert[_, _]): List[Long]

    inline def queryMap(query: SelectQuery[_]): List[Map[String, Any]]

    inline def queryTuple[T <: Tuple](query: SelectQuery[T]): List[T]

    inline def query[T <: Product](query: SelectQuery[_]): List[T]

    inline def queryMap(sql: String): List[Map[String, Any]]

    inline def findMap(query: Select[_]): Option[Map[String, Any]]

    inline def findTuple[T <: Tuple](query: Select[T]): Option[T]

    inline def find[T <: Product](pk: SqlDataType | Tuple): Option[T]

    inline def queryPageOfMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]]

    inline def queryPageOfTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T]

    inline def queryPage[T <: Product](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T]

    inline def fetchCount(query: Select[_]): Int
}