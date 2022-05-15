package org.easysql.ast.order

import org.easysql.ast.SqlNode
import org.easysql.ast.expr.SqlExpr

case class SqlOrderBy(expr: SqlExpr, order: SqlOrderByOption) extends SqlNode