package org.easysql.ast.statement.select

import org.easysql.ast.SqlNode
import org.easysql.ast.expr.SqlExpr

case class SqlSelectItem(expr: SqlExpr, alias: Option[String] = None) extends SqlNode
