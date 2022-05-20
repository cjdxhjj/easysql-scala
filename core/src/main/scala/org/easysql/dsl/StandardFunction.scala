package org.easysql.dsl

import org.easysql.ast.SqlSingleConstType
import org.easysql.query.select.SelectQuery

def count() = AggFunctionExpr[Int]("COUNT", List())

def count(query: Expr[_]) = AggFunctionExpr[Int]("COUNT", List(query))

def countDistinct(query: Expr[_]) = AggFunctionExpr[Int]("COUNT", List(query), true)

def sum[T <: Int | Long | Float | Double | Null](query: Expr[T]) = AggFunctionExpr[T]("SUM", List(query))

def avg[T <: Int | Long | Float | Double | Null](query: Expr[T]) = AggFunctionExpr[T]("AVG", List(query))

def max[T <: SqlSingleConstType | Null](query: Expr[T]) = AggFunctionExpr[T]("MAX", List(query))

def min[T <: SqlSingleConstType | Null](query: Expr[T]) = AggFunctionExpr[T]("MIN", List(query))

def rank() = AggFunctionExpr[Int]("RANK", List())

def denseRank() = AggFunctionExpr[Int]("DENSE_RANK", List())

def rowNumber() = AggFunctionExpr[Int]("ROW_NUMBER", List())

def cube(expr: Expr[_]*) = NormalFunctionExpr("CUBE", expr.toList)

def rollup(expr: Expr[_]*) = NormalFunctionExpr("ROLLUP", expr.toList)

def groupingSets(expr: (Expr[_] | Tuple)*) = {
    val name = "GROUPING SETS"
    val args = expr.toList.map {
        case e: Expr[_] => ListExpr(List(e))
        case t: Tuple => {
            val list = t.toList
            val exprList: List[Expr[_]] = list.map {
                case v: SqlSingleConstType => const(v)
                case expr: Expr[_] => expr
                case _ => const(null)
            }
            ListExpr(exprList)
        }
    }
    NormalFunctionExpr(name, args)
}