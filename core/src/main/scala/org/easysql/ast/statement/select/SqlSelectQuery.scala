package org.easysql.ast.statement.select

import org.easysql.ast.expr.*
import org.easysql.ast.order.SqlOrderBy
import org.easysql.ast.statement.SqlStatement
import org.easysql.ast.table.SqlTableSource
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.select.*
import org.easysql.ast.statement.select.SqlSelectItem.SqlSelectItemFromExpr

import scala.quoted.*
import scala.collection.mutable.ListBuffer

sealed class SqlSelectQuery extends SqlStatement

object SqlSelectQuery {
    given FromExpr[SqlSelectQuery] with {
        override def unapply(x: Expr[SqlSelectQuery])(using Quotes): Option[SqlSelectQuery] = x match {
            // todo
            case '{ $x: SqlSelect } => x.value
//            case '{ $x: SqlUnionSelect } => x.value
//            case '{ $x: SqlWithSelect } => x.value
//            case '{ $x: SqlValuesSelect } => x.value
            case _ => None
        }
    }
}

case class SqlSelect(var distinct: Boolean,
                     var selectList: List[SqlSelectItem],
                     var from: Option[SqlTableSource],
                     var where: Option[SqlExpr],
                     var groupBy: List[SqlExpr],
                     var orderBy: List[SqlOrderBy],
                     var forUpdate: Boolean,
                     var limit: Option[SqlLimit],
                     var having: Option[SqlExpr]
                    ) extends SqlSelectQuery() {
    def addSelectItem(expr: SqlExpr | String, alias: Option[String] = None): Unit = {
        val sqlExpr = expr match {
            case e: SqlExpr => e
            case s: String => SqlIdentifierExpr(s)
        }
        selectList = selectList ::: List(SqlSelectItem(sqlExpr, alias))
    }

    def addCondition(condition: SqlExpr): Unit = {
        where = if (where.isEmpty) {
            Some(condition)
        } else  {
            Some(SqlBinaryExpr(where.get, SqlBinaryOperator.AND, condition))
        }
    }

    def addHaving(condition: SqlExpr): Unit = {
        having = if (having.isEmpty) {
            Some(condition)
        } else  {
            Some(SqlBinaryExpr(having.get, SqlBinaryOperator.AND, condition))
        }
    }
}

object SqlSelect {
    given FromExpr[SqlSelect] with {
        override def unapply(x: Expr[SqlSelect])(using Quotes): Option[SqlSelect] = x match {
            case '{ SqlSelect(${Expr(d)}, ${Expr(s)}, ${Expr(f)}, ${Expr(w)}, ${Expr(g)}, ${Expr(o)}, ${Expr(fu)}, ${Expr(l)}, ${Expr(h)}) } => Some(SqlSelect(d, s, f, w, g, o, fu, l, h))
            case '{ new SqlSelect(${Expr(d)}, ${Expr(s)}, ${Expr(f)}, ${Expr(w)}, ${Expr(g)}, ${Expr(o)}, ${Expr(fu)}, ${Expr(l)}, ${Expr(h)}) } => Some(SqlSelect(d, s, f, w, g, o, fu, l, h))
            case _ => None
        }
    }
}

case class SqlUnionSelect(left: SqlSelectQuery, unionType: SqlUnionType, right: SqlSelectQuery) extends SqlSelectQuery()

case class SqlWithSelect(withList: ListBuffer[SqlWithItem] = ListBuffer(), var recursive: Boolean = false, var query: Option[SqlSelectQuery] = None) extends SqlSelectQuery()

case class SqlValuesSelect(values: ListBuffer[List[SqlExpr]] = ListBuffer()) extends SqlSelectQuery()