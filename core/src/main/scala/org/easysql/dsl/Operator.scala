package org.easysql.dsl

import org.easysql.ast.expr.SqlBinaryOperator
import org.easysql.query.select.SelectQuery
import org.easysql.ast.SqlSingleConstType

extension[T <: Nothing] (e: Expr[T]) {
    def +(value: SqlSingleConstType): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.ADD, const(value))

    def +(expr: Expr[_]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.ADD, expr)

    def +(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.ADD, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def -(value: SqlSingleConstType): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.SUB, const(value))

    def -(expr: Expr[_]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.SUB, expr)

    def -(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.SUB, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def *(value: SqlSingleConstType): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MUL, const(value))

    def *(expr: Expr[_]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MUL, expr)

    def *(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MUL, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def /(value: SqlSingleConstType): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.DIV, const(value))

    def /(expr: Expr[_]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.DIV, expr)

    def /(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.DIV, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def %(value: SqlSingleConstType): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MOD, const(value))

    def %(expr: Expr[_]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MOD, expr)

    def %(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T] = BinaryExpr[T](e, SqlBinaryOperator.MOD, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def ==(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, const(value))

    def ==(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, expr)

    def ==(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def ===(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, const(value))

    def ===(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, expr)

    def ===(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.EQ, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <>(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.NE, const(value))

    def <>(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.NE, expr)

    def <>(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.NE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def >(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GT, const(value))

    def >(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GT, expr)

    def >(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GT, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def >=(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GE, const(value))

    def >=(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GE, expr)

    def >=(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.GE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LT, const(value))

    def <(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LT, expr)

    def <(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LT, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <=(value: SqlSingleConstType): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LE, const(value))

    def <=(expr: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LE, expr)

    def <=(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.LE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    infix def &&(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.AND, query)

    infix def ||(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.OR, query)

    infix def ^(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.XOR, query)

    infix def in(list: List[SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]]]): Expr[Boolean] = {
        InListExpr(e, list)
    }

    infix def in(list: (SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]])*): Expr[Boolean] = {
        InListExpr(e, list.toList)
    }

    infix def notIn(list: List[SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]]]): Expr[Boolean] = {
        InListExpr(e, list, true)
    }

    infix def notIn(list: (SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]])*): Expr[Boolean] = {
        InListExpr(e, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[_]]): Expr[Boolean] = InSubQueryExpr(e, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[_]]): Expr[Boolean] = InSubQueryExpr(e, subQuery, true)

    infix def between(between: Tuple2[SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]], SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]]]): Expr[Boolean] = {
        BetweenExpr(e, between._1, between._2)
    }

    infix def notBetween(between: Tuple2[SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]], SqlSingleConstType | Expr[_] | SelectQuery[Tuple1[_]]]): Expr[Boolean] = {
        BetweenExpr(e, between._1, between._2, true)
    }
}
