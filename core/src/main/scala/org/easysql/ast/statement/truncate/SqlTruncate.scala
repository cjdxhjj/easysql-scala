package org.easysql.ast.statement.truncate

import org.easysql.ast.expr.SqlExpr
import org.easysql.ast.statement.SqlStatement

case class SqlTruncate(var table: Option[SqlExpr] = None) extends SqlStatement
