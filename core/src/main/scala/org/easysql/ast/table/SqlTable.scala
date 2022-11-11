package org.easysql.ast.table

import org.easysql.ast.SqlNode
import org.easysql.ast.statement.select.SqlSelectQuery
import org.easysql.ast.expr.SqlExpr

import scala.collection.mutable.ListBuffer

sealed class SqlTable(
    var alias: Option[String] = None, 
    var columnAliasNames: ListBuffer[String] = ListBuffer()
) extends SqlNode

case class SqlIdentTable(tableName: String) extends SqlTable()

case class SqlSubQueryTable(
    select: SqlSelectQuery,
    var isLateral: Boolean = false
) extends SqlTable()

case class SqlJoinTable(
    left: SqlTable,
    joinType: SqlJoinType,
    right: SqlTable,
    var on: Option[SqlExpr] = None
) extends SqlTable()
