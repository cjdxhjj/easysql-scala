package org.easysql.dsl

import org.easysql.ast.{SqlDataType, SqlNumberType}
import org.easysql.query.select.SelectQuery

def count() = AggFunctionExpr[Int]("COUNT", List())

def count(query: Expr[_, _]) = AggFunctionExpr[Int]("COUNT", List(query))

def countDistinct(query: Expr[_, _]) = AggFunctionExpr[Int]("COUNT", List(query), true)

def sum[T <: SqlDataType | Null](query: Expr[T, _]) = AggFunctionExpr[T]("SUM", List(query))

def avg[T <: SqlDataType | Null](query: Expr[T, _]) = AggFunctionExpr[T]("AVG", List(query))

def max[T <: SqlDataType | Null](query: Expr[T, _]) = AggFunctionExpr[T]("MAX", List(query))

def min[T <: SqlDataType | Null](query: Expr[T, _]) = AggFunctionExpr[T]("MIN", List(query))

def rank() = AggFunctionExpr[Int]("RANK", List())

def denseRank() = AggFunctionExpr[Int]("DENSE_RANK", List())

def rowNumber() = AggFunctionExpr[Int]("ROW_NUMBER", List())

def cube(expr: Expr[_, _]*) = NormalFunctionExpr("CUBE", expr.toList)

def rollup(expr: Expr[_, _]*) = NormalFunctionExpr("ROLLUP", expr.toList)

def groupingSets(expr: (Expr[_, _] | Tuple)*) = {
    val name = "GROUPING SETS"
    val args = expr.toList.map {
        case e: Expr[_, _] => ListExpr(List(e))
        case t: Tuple =>
            val list = t.toList
            val exprList: List[Expr[_, _]] = list.map {
                case v: SqlDataType => const(v)
                case expr: Expr[_, _] => expr
                case _ => const(null)
            }
            ListExpr(exprList)
    }
    NormalFunctionExpr(name, args)
}