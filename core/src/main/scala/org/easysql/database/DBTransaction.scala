package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.dsl.MapUnionNull

trait DBTransaction(db: DB) {
    def run(query: ReviseQuery): Int

    def runAndReturnKey(query: Insert[_, _]): List[Long]

    def queryMap(query: SelectQuery[_]): List[Map[String, Any]]

    def queryTuple[T <: Tuple](query: SelectQuery[T]): List[MapUnionNull[T]]

    def findMap(query: Select[_]): Option[Map[String, Any]]

    def findTuple[T <: Tuple](query: Select[T]): Option[MapUnionNull[T]]

    def pageMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]]

    def pageTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[MapUnionNull[T]]
    
    def fetchCount(query: Select[_]): Int
}
