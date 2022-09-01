package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.dsl.*

import scala.reflect.ClassTag

trait DBOperater {
//    def run(query: ReviseQuery): Int
//
//    def runAndReturnKey(query: Insert[_, _]): List[Long]
//
//    def queryMap(query: SelectQuery[_]): List[Map[String, Any]]
//
//    def queryTuple[T <: Tuple](query: SelectQuery[T]): List[T]
//
//    def query[T <: TableEntity[_]](query: SelectQuery[_])(using t: TableSchema[T], ct: ClassTag[T]): List[T]
//
//    def queryMap(sql: String): List[Map[String, Any]]
//
//    def findMap(query: Select[_]): Option[Map[String, Any]]
//
//    def findTuple[T <: Tuple](query: Select[T]): Option[T]
//
//    def find[T <: TableEntity[_]](pk: PK[T])(using t: TableSchema[T], ct: ClassTag[T]): Option[T]
//
//    def queryPageOfMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]]
//
//    def queryPageOfTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T]
//
//    def queryPage[T <: TableEntity[_]](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true)(using t: TableSchema[T], ct: ClassTag[T]): Page[T]
//
//    def fetchCount(query: Select[_]): Int
}
