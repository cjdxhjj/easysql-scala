package org.easysql.ast.limit

import org.easysql.ast.SqlNode

import scala.quoted.*

case class SqlLimit(var limit: Int, var offset: Int) extends SqlNode

object SqlLimit {
    given FromExpr[SqlLimit] with {
        override def unapply(x: Expr[SqlLimit])(using Quotes): Option[SqlLimit] = x match {
            case '{ SqlLimit(${Expr(l)}, ${Expr(o)}) } => Some(SqlLimit(l, o))
            case _ => None
        }
    }
}