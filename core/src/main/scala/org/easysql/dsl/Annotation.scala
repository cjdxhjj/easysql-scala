package org.easysql.dsl

import scala.annotation.StaticAnnotation

@scala.annotation.meta.field
case class Table(tableName: String = "") extends StaticAnnotation

@scala.annotation.meta.field
case class PrimaryKey(columnName: String = "") extends StaticAnnotation

@scala.annotation.meta.field
case class IncrKey(columnName: String = "") extends StaticAnnotation

@scala.annotation.meta.field
case class Column(columnName: String = "") extends StaticAnnotation
