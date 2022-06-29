package org.easysql.dsl

import org.easysql.ast.expr.SqlBinaryOperator
import org.easysql.query.select.SelectQuery
import org.easysql.ast.SqlSingleConstType

extension[T <: Nothing] (e: Expr[T, _]) {
    def +(value: SqlSingleConstType): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

    def +(expr: Expr[_, _]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

    def +(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.ADD, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def -(value: SqlSingleConstType): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

    def -(expr: Expr[_, _]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

    def -(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.SUB, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def *(value: SqlSingleConstType): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

    def *(expr: Expr[_, _]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

    def *(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MUL, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def /(value: SqlSingleConstType): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

    def /(expr: Expr[_, _]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

    def /(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.DIV, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def %(value: SqlSingleConstType): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

    def %(expr: Expr[_, _]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)

    def %(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[T, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.MOD, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def ==(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, const(value))

    def ==(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, expr)

    def ==(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def ===(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, const(value))

    def ===(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, expr)

    def ===(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.EQ, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <>(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.NE, const(value))

    def <>(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.NE, expr)

    def <>(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.NE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def >(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GT, const(value))

    def >(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GT, expr)

    def >(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GT, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def >=(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GE, const(value))

    def >=(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GE, expr)

    def >=(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.GE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LT, const(value))

    def <(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LT, expr)

    def <(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LT, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    def <=(value: SqlSingleConstType): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LE, const(value))

    def <=(expr: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LE, expr)

    def <=(subQuery: SelectQuery[Tuple1[_]]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.LE, SubQueryExpr(subQuery.asInstanceOf[SelectQuery[Tuple1[Nothing]]]))

    infix def &&(query: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.AND, query)

    infix def ||(query: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.OR, query)

    infix def ^(query: Expr[_, _]): BinaryExpr[Boolean, EmptyTuple] = BinaryExpr(e, SqlBinaryOperator.XOR, query)

    infix def in(list: List[SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]]]): Expr[Boolean, EmptyTuple] = {
        InListExpr(e, list)
    }

    infix def in(list: (SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]])*): Expr[Boolean, EmptyTuple] = {
        InListExpr(e, list.toList)
    }

    infix def notIn(list: List[SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]]]): Expr[Boolean, EmptyTuple] = {
        InListExpr(e, list, true)
    }

    infix def notIn(list: (SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]])*): Expr[Boolean, EmptyTuple] = {
        InListExpr(e, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[_]]): Expr[Boolean, EmptyTuple] = InSubQueryExpr(e, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[_]]): Expr[Boolean, EmptyTuple] = InSubQueryExpr(e, subQuery, true)

    infix def between(between: (SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]], SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]])): Expr[Boolean, EmptyTuple] = {
        BetweenExpr(e, between._1, between._2)
    }

    infix def notBetween(between: (SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]], SqlSingleConstType | Expr[_, _] | SelectQuery[Tuple1[_]])): Expr[Boolean, EmptyTuple] = {
        BetweenExpr(e, between._1, between._2, true)
    }
}
