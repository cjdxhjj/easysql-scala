package org.easysql.ast.statement.truncate

import org.easysql.ast.statement.SqlStatement
import org.easysql.ast.table.SqlIdentTable

case class SqlTruncate(var table: Option[SqlIdentTable] = None) extends SqlStatement
