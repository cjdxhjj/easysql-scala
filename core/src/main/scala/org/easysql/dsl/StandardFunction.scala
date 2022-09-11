package org.easysql.dsl

import org.easysql.ast.{SqlDataType, SqlNumberType}
import org.easysql.query.select.SelectQuery

def count() = AggFunctionExpr[Long]("COUNT", List())

def count(query: Expr[_]) = AggFunctionExpr[Long]("COUNT", List(query))

def countDistinct(query: Expr[_]) = AggFunctionExpr[Long]("COUNT", List(query), true)

def sum[T <: SqlNumberType](query: Expr[T]) = AggFunctionExpr[Number]("SUM", List(query))

def avg[T <: SqlNumberType](query: Expr[T]) = AggFunctionExpr[Number]("AVG", List(query))

def max[T <: SqlNumberType](query: Expr[T]) = AggFunctionExpr[T]("MAX", List(query))

def min[T <: SqlNumberType](query: Expr[T]) = AggFunctionExpr[T]("MIN", List(query))

def rank() = AggFunctionExpr[Number]("RANK", List())

def denseRank() = AggFunctionExpr[Number]("DENSE_RANK", List())

def rowNumber() = AggFunctionExpr[Number]("ROW_NUMBER", List())

def cube(expr: Expr[_]*) = NormalFunctionExpr("CUBE", expr.toList)

def rollup(expr: Expr[_]*) = NormalFunctionExpr("ROLLUP", expr.toList)

def groupingSets(expr: (Expr[_] | Tuple)*) = {
    val name = "GROUPING SETS"
    val args = expr.toList.map {
        case e: Expr[_] => ListExpr(List(e))
        case t: Tuple =>
            val list = t.toList
            val exprList: List[Expr[_]] = list.map {
                case v: SqlDataType => const(v)
                case expr: Expr[_] => expr
                case _ => const(null)
            }
            ListExpr(exprList)
    }
    NormalFunctionExpr(name, args)
}