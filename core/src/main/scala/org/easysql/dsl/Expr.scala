package org.easysql.dsl

import org.easysql.ast.expr.{SqlBinaryOperator, SqlExpr, SqlSubQueryPredicate}
import org.easysql.visitor.*
import org.easysql.ast.order.SqlOrderByOption
import org.easysql.dsl.const
import org.easysql.query.select.SelectQuery
import org.easysql.ast.{SqlDataType, SqlNumberType}
import org.easysql.util.anyToExpr

import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.Tuple.Concat

trait SelectItem[T]

case class AliasExpr[T <: SqlDataType, Alias <: String](expr: Expr[T], name: Alias) extends SelectItem[T]

sealed trait Expr[T <: SqlDataType] extends SelectItem[T] {
    def ===[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, const(value))

    def ===[V <: T](value: Option[V]): BinaryExpr[Boolean] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.EQ, const(null))
        }
    }

    def ===[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, expr)

    def ===[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def equal(expr: Any): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, anyToExpr(expr))

    def <>[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, const(value))

    def <>[V <: T](value: Option[V]): BinaryExpr[Boolean] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.NE, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.NE, const(null))
        }
    }

    def <>[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, expr)

    def <>[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, SubQueryExpr(subQuery))

    def >[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, const(value))

    def >[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, expr)

    def >[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, SubQueryExpr(subQuery))

    def >=[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, const(value))

    def >=[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, expr)

    def >=[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, SubQueryExpr(subQuery))

    def <[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, const(value))

    def <[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, expr)

    def <[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, SubQueryExpr(subQuery))

    def <=[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, const(value))

    def <=[V <: T](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, expr)

    def <=[V <: T](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, SubQueryExpr(subQuery))

    def &&(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.AND, query)

    def ||(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.OR, query)

    def ^(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.XOR, query)

    def unary_! : NormalFunctionExpr[Boolean] = NormalFunctionExpr("NOT", List(this))

    infix def in[V <: T](list: List[V | Expr[V] | SelectQuery[Tuple1[V], _]]): Expr[Boolean] = {
        if (list.isEmpty) {
            const(false)
        } else {
            InListExpr(this, list)
        }
    }

    infix def in[V <: T](list: (V | Expr[V] | SelectQuery[Tuple1[V], _])*): Expr[Boolean] = {
        InListExpr(this, list.toList)
    }

    infix def notIn[V <: T](list: List[V | Expr[V] | SelectQuery[Tuple1[V], _]]): Expr[Boolean] = {
        if (list.isEmpty) {
            const(true)
        } else {
            InListExpr(this, list, true)
        }
    }

    infix def notIn[V <: T](list: (V | Expr[V] | SelectQuery[Tuple1[V], _])*): Expr[Boolean] = {
        InListExpr(this, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[T], _]): Expr[Boolean] = InSubQueryExpr(this, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[T], _]): Expr[Boolean] = InSubQueryExpr(this, subQuery, true)

    infix def between[V <: T](between: (V | Expr[V] | SelectQuery[Tuple1[V], _], V | Expr[V] | SelectQuery[Tuple1[V], _])): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2)
    }

    infix def notBetween[V <: T](between: (V | Expr[V] | SelectQuery[Tuple1[V], _], V | Expr[V] | SelectQuery[Tuple1[V], _])): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2, true)
    }

    def asc: OrderBy = OrderBy(this, SqlOrderByOption.ASC)

    def desc: OrderBy = OrderBy(this, SqlOrderByOption.DESC)

    inline infix def as(inline name: String)(using NonEmpty[name.type] =:= Any) = AliasExpr[T, name.type](this, name)

    infix def unsafeAs(name: String) = AliasExpr[T, name.type](this, name)
}

extension [T <: SqlNumberType] (e: Expr[T]) {
    def +[V <: SqlNumberType](value: V): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

    def +[V <: SqlNumberType](expr: Expr[V]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

    def +[V <: SqlNumberType](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.ADD, SubQueryExpr(subQuery))

    def -[V <: SqlNumberType](value: V): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

    def -[V <: SqlNumberType](expr: Expr[V]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

    def -[V <: SqlNumberType](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.SUB, SubQueryExpr(subQuery))

    def *[V <: SqlNumberType](value: V): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

    def *[V <: SqlNumberType](expr: Expr[V]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

    def *[V <: SqlNumberType](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MUL, SubQueryExpr(subQuery))

    def /[V <: SqlNumberType](value: V): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

    def /[V <: SqlNumberType](expr: Expr[V]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

    def /[V <: SqlNumberType](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.DIV, SubQueryExpr(subQuery))

    def %[V <: SqlNumberType](value: V): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

    def %[V <: SqlNumberType](expr: Expr[V]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)

    def %[V <: SqlNumberType](subQuery: SelectQuery[Tuple1[V], _]): BinaryExpr[Number] = BinaryExpr(e, SqlBinaryOperator.MOD, SubQueryExpr(subQuery))
}

extension [T <: String] (e: Expr[T]) {
    infix def like(value: String): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, const(value))
    }

    infix def like(expr: Expr[String]): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, expr)
    }

    infix def notLike(value: String): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, const(value))
    }

    infix def notLike(expr: Expr[String]): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, expr)
    }
}

case class ConstExpr[T <: SqlDataType](value: T) extends Expr[T]()

case class BinaryExpr[T <: SqlDataType](
    left: Expr[_],
    operator: SqlBinaryOperator,
    right: Expr[_]
) extends Expr[T]() {
    def thenIs[TV <: SqlDataType](thenValue: TV | Expr[TV] | SelectQuery[Tuple1[TV], _]): CaseBranch[TV] =
        CaseBranch(this, thenValue)
}

case class ColumnExpr[T <: SqlDataType](column: String) extends Expr[T]()

case class TableColumnExpr[T <: SqlDataType](
    table: String,
    column: String,
    schema: TableSchema[_]
) extends Expr[T]()

case class PrimaryKeyColumnExpr[T <: SqlDataType](
    table: String,
    column: String,
    schema: TableSchema[_],
    isIncr: Boolean = false
) extends Expr[T]()

case class SubQueryExpr[T <: SqlDataType](selectQuery: SelectQuery[Tuple1[T], _]) extends Expr[T]()

case class NormalFunctionExpr[T <: SqlDataType](name: String, args: List[Expr[_]]) extends Expr[T]()

case class AggFunctionExpr[T <: SqlDataType](
    name: String,
    args: List[Expr[_]],
    distinct: Boolean = false,
    attributes: Map[String, Expr[_]] = Map(),
    orderBy: List[OrderBy] = List()
) extends Expr[T]() {
    def over: OverExpr[T] = OverExpr(this)
}

case class CaseExpr[T <: SqlDataType](
    conditions: List[CaseBranch[T]],
    default: T | Expr[T] | SelectQuery[Tuple1[T], _] = null
) extends Expr[T]() {
    infix def elseIs(value: T | Expr[T] | SelectQuery[Tuple1[T], _]): CaseExpr[T] =
        if value != null then CaseExpr(this.conditions, value) else this
}

case class ListExpr[T <: SqlDataType](list: List[T | Expr[_] | SelectQuery[_, _]]) extends Expr[T]()

case class InListExpr[T <: SqlDataType](
    query: Expr[_],
    list: List[T | Expr[_] | SelectQuery[_, _]],
    isNot: Boolean = false
) extends Expr[Boolean]()

case class InSubQueryExpr[T <: SqlDataType](
    query: Expr[T], 
    subQuery: SelectQuery[_, _], 
    isNot: Boolean = false
) extends Expr[Boolean]()

case class CastExpr[T <: SqlDataType](query: Expr[_], castType: String) extends Expr[T]()

case class BetweenExpr[T <: SqlDataType](
    query: Expr[_],
    start: T | Expr[_] | SelectQuery[_, _],
    end: T | Expr[_] | SelectQuery[_, _],
    isNot: Boolean = false
) extends Expr[Boolean]()

case class AllColumnExpr(owner: Option[String] = None) extends Expr[Nothing]()

case class OverExpr[T <: SqlDataType](
    function: AggFunctionExpr[_],
    partitionBy: ListBuffer[Expr[_]] = ListBuffer(),
    orderBy: ListBuffer[OrderBy] = ListBuffer()
) extends Expr[T]() {
    def partitionBy(query: Expr[_]*): OverExpr[T] = OverExpr(this.function, this.partitionBy.addAll(query), this.orderBy)

    def orderBy(order: OrderBy*): OverExpr[T] = OverExpr(this.function, this.partitionBy, this.orderBy.addAll(order))
}

case class SubQueryPredicateExpr[T <: SqlDataType](query: SelectQuery[_, _], predicate: SqlSubQueryPredicate) extends Expr[T]()

case class CaseBranch[T <: SqlDataType](query: Expr[_], thenValue: T | Expr[T] | SelectQuery[Tuple1[T], _])

case class OrderBy(query: Expr[_], order: SqlOrderByOption)
