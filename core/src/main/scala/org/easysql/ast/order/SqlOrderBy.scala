package org.easysql.ast.order

import org.easysql.ast.SqlNode
import org.easysql.ast.expr.SqlExpr

import scala.quoted.*

case class SqlOrderBy(expr: SqlExpr, order: SqlOrderByOption) extends SqlNode

object SqlOrderBy {
    given SqlOrderByFromExpr: FromExpr[SqlOrderBy] with {
        override def unapply(x: Expr[SqlOrderBy])(using Quotes): Option[SqlOrderBy] = x match {
            case '{ SqlOrderBy(${Expr(e)}, ${Expr(o)}) } => Some(SqlOrderBy(e, o))
            case _ => None
        }
    }
}