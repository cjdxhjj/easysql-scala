package org.easysql.ast.limit

import org.easysql.ast.SqlNode

case class SqlLimit(var limit: Int, var offset: Int) extends SqlNode
