package org.easysql.ast.expr

import org.easysql.ast.SqlNode
import org.easysql.ast.order.SqlOrderBy
import org.easysql.ast.statement.select.SqlSelectQuery

import java.text.SimpleDateFormat
import java.util.Date

sealed class SqlExpr extends SqlNode

case class SqlBinaryExpr(left: SqlExpr, operator: SqlBinaryOperator, right: SqlExpr) extends SqlExpr

case class SqlIdentExpr(name: String) extends SqlExpr

case class SqlPropertyExpr(owner: String, name: String) extends SqlExpr

case class SqlNullExpr() extends SqlExpr {
    override def toString = "NULL"
}

case class SqlAllColumnExpr(owner: Option[String] = None) extends SqlExpr

case class SqlNumberExpr(number: Number) extends SqlExpr {
    override def toString: String = number.toString
}

case class SqlDateExpr(date: Date) extends SqlExpr {
    override def toString: String = {
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        s"'${format.format(date)}'"
    }
}

case class SqlCharExpr(text: String) extends SqlExpr {
    override def toString = s"'${text.replace("'", "''")}'"
}

case class SqlBooleanExpr(boolean: Boolean) extends SqlExpr {
    override def toString: String = boolean.toString
}

case class SqlListExpr[T <: SqlExpr](items: List[T] = List()) extends SqlExpr

case class SqlAggFunctionExpr(
    name: String, 
    args: List[SqlExpr] = List(), 
    distinct: Boolean = false, 
    attributes: Map[String, SqlExpr] = Map(),
    orderBy: List[SqlOrderBy] = List()
) extends SqlExpr

case class SqlExprFunctionExpr(name: String, var args: List[SqlExpr] = List()) extends SqlExpr

case class SqlCastExpr(expr: SqlExpr, castType: String) extends SqlExpr

case class SqlSelectQueryExpr(query: SqlSelectQuery) extends SqlExpr

case class SqlInExpr(expr: SqlExpr, inExpr: SqlExpr, isNot: Boolean = false) extends SqlExpr

case class SqlBetweenExpr[T <: SqlExpr](expr: SqlExpr, start: T, end: T, isNot: Boolean = false) extends SqlExpr

case class SqlOverExpr(agg: SqlAggFunctionExpr, partitionBy: List[SqlExpr] = List(), orderBy: List[SqlOrderBy] = List()) extends SqlExpr

case class SqlCaseExpr(caseList: List[SqlCase] = List(), default: SqlExpr) extends SqlExpr

case class SqlSubQueryPredicateExpr(select: SqlSelectQueryExpr, predicate: SqlSubQueryPredicate) extends SqlExpr
