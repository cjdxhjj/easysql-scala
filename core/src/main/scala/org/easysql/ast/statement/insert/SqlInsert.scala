package org.easysql.ast.statement.insert

import org.easysql.ast.expr.SqlExpr
import org.easysql.ast.statement.SqlStatement
import org.easysql.ast.statement.select.SqlSelectQuery

import scala.collection.mutable.ListBuffer

case class SqlInsert(var table: Option[SqlExpr] = None,
                     columns: ListBuffer[SqlExpr] = ListBuffer(),
                     values: ListBuffer[List[SqlExpr]] = ListBuffer(),
                     var query: Option[SqlSelectQuery] = None) extends SqlStatement
