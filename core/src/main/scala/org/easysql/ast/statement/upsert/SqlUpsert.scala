package org.easysql.ast.statement.upsert

import org.easysql.ast.expr.SqlExpr
import org.easysql.ast.statement.SqlStatement

import scala.collection.mutable.ListBuffer

case class SqlUpsert(var table: Option[SqlExpr] = None, 
                     columns: ListBuffer[SqlExpr]= ListBuffer(), 
                     value : ListBuffer[SqlExpr] = ListBuffer(),
                     primaryColumns: ListBuffer[SqlExpr] = ListBuffer(), 
                     updateColumns: ListBuffer[SqlExpr] = ListBuffer()) extends SqlStatement