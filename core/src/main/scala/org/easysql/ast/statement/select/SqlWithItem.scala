package org.easysql.ast.statement.select

import org.easysql.ast.expr.SqlExpr
import org.easysql.ast.SqlNode

case class SqlWithItem(name: SqlExpr, query: SqlSelectQuery, columns: List[SqlExpr]) extends SqlNode
