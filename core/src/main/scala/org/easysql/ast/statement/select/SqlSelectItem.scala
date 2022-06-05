package org.easysql.ast.statement.select

import org.easysql.ast.SqlNode
import org.easysql.ast.expr.SqlExpr

import scala.quoted.*

case class SqlSelectItem(expr: SqlExpr, alias: Option[String]) extends SqlNode

object SqlSelectItem {
    given SqlSelectItemFromExpr: FromExpr[SqlSelectItem] with {
        override def unapply(x: Expr[SqlSelectItem])(using Quotes): Option[SqlSelectItem] = x match {
            case '{ SqlSelectItem(${Expr(e)}: SqlExpr, ${Expr(a)}) } => Some(SqlSelectItem(e, a))
            case _ => None
        }
    }
}
