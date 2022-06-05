package org.easysql.ast.statement.select

import org.easysql.ast.expr.SqlExpr
import org.easysql.ast.SqlNode

import scala.quoted.*

case class SqlWithItem(name: SqlExpr, query: SqlSelectQuery, columns: List[SqlExpr]) extends SqlNode

object SqlWithItem {
    given SqlWithItemFromExpr: FromExpr[SqlWithItem] with {
        override def unapply(x: Expr[SqlWithItem])(using Quotes): Option[SqlWithItem] = x match {
            case '{ SqlWithItem(${Expr(n)}, ${Expr(q)}, ${Expr(c)}) } => Some(SqlWithItem(n, q, c))
            case _ => None
        }
    }
}