package org.easysql.database

case class Page[T](totalPage: Int = 0, totalCount: Int = 0, data: List[T] = List())
