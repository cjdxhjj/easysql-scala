package org.easysql.ast.table

import org.easysql.ast.SqlNode
import org.easysql.ast.statement.select.SqlSelectQuery
import org.easysql.ast.expr.SqlExpr

import scala.quoted.*
import scala.collection.mutable.ListBuffer

sealed class SqlTableSource(val alias: Option[String], val columnAliasNames: List[String]) extends SqlNode

object SqlTableSource {
    given SqlTableSourceFromExpr: FromExpr[SqlTableSource] with {
        override def unapply(x: Expr[SqlTableSource])(using Quotes): Option[SqlTableSource] = x match {
            case '{ $x: SqlIdentifierTableSource } => x.value
            case '{ $x: SqlSubQueryTableSource } => x.value
            case '{ $x: SqlJoinTableSource } => x.value
            case _ => None
        }
    }
}

case class SqlIdentifierTableSource(tableName: String, override val alias: Option[String], override val columnAliasNames: List[String]) extends SqlTableSource(alias, columnAliasNames)

object SqlIdentifierTableSource {
    given FromExpr[SqlIdentifierTableSource] with {
        override def unapply(x: Expr[SqlIdentifierTableSource])(using Quotes): Option[SqlIdentifierTableSource] = x match {
            case '{ SqlIdentifierTableSource(${Expr(t)}, ${Expr(a)}, ${Expr(c)}) } => Some(SqlIdentifierTableSource(t, a, c))
            case _ => None
        }
    }
}

case class SqlSubQueryTableSource(select: SqlSelectQuery, isLateral: Boolean, override val alias: Option[String], override val columnAliasNames: List[String]) extends SqlTableSource(alias, columnAliasNames)

object SqlSubQueryTableSource {
    given FromExpr[SqlSubQueryTableSource] with {
        override def unapply(x: Expr[SqlSubQueryTableSource])(using Quotes): Option[SqlSubQueryTableSource] = x match {
            case '{ SqlSubQueryTableSource(${Expr(s)}, ${Expr(i)}, ${Expr(a)}, ${Expr(c)}) } => Some(SqlSubQueryTableSource(s, i, a, c))
            case _ => None
        }
    }
}

case class SqlJoinTableSource(left: SqlTableSource,
                              joinType: SqlJoinType,
                              right: SqlTableSource,
                              var on: Option[SqlExpr],
                              override val alias: Option[String],
                              override val columnAliasNames: List[String]) extends SqlTableSource(alias, columnAliasNames)

object SqlJoinTableSource {
    given SqlJoinTableSourceFromExpr: FromExpr[SqlJoinTableSource] with {
        override def unapply(x: Expr[SqlJoinTableSource])(using Quotes): Option[SqlJoinTableSource] = x match {
            case '{ SqlJoinTableSource(${Expr(l)}, ${Expr(j)}, ${Expr(r)}, ${Expr(o)}, ${Expr(a)}, ${Expr(c)}) } => Some(SqlJoinTableSource(l, j, r, o, a, c))
            case _ => None
        }
    }
}
