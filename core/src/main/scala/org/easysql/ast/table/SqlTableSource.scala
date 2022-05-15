package org.easysql.ast.table

import org.easysql.ast.SqlNode
import org.easysql.ast.statement.select.SqlSelectQuery
import org.easysql.ast.expr.SqlExpr

import scala.collection.mutable.ListBuffer

sealed class SqlTableSource(var alias: Option[String] = None, var columnAliasNames: ListBuffer[String] = ListBuffer()) extends SqlNode

case class SqlIdentifierTableSource(tableName: String) extends SqlTableSource()

case class SqlSubQueryTableSource(select: SqlSelectQuery,
                                  var isLateral: Boolean = false) extends SqlTableSource()

case class SqlJoinTableSource(left: SqlTableSource,
                              joinType: SqlJoinType,
                              right: SqlTableSource,
                              var on: Option[SqlExpr] = None) extends SqlTableSource()
