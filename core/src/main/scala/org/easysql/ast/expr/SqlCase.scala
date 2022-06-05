package org.easysql.ast.expr

import scala.quoted.*

case class SqlCase(expr: SqlExpr, thenExpr: SqlExpr)

object SqlCase {
    given SqlCaseFromExpr: FromExpr[SqlCase] with {
        override def unapply(x: Expr[SqlCase])(using Quotes): Option[SqlCase] = x match {
            case '{ SqlCase(${Expr(e)}, ${Expr(t)}) } => Some(SqlCase(e, t))
            case _ => None
        }
    }
}