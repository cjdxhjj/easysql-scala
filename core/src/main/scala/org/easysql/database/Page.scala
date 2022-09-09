package org.easysql.database

case class Page[T](totalPage: Long = 0l, totalCount: Long = 0l, data: List[T] = List())
