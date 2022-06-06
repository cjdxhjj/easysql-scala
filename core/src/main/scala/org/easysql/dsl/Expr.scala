package org.easysql.dsl

import org.easysql.ast.expr.{SqlBinaryOperator, SqlExpr, SqlSubQueryPredicate}
import org.easysql.visitor.*
import org.easysql.ast.order.SqlOrderByOption
import org.easysql.dsl.const
import org.easysql.query.select.SelectQuery
import org.easysql.ast.SqlSingleConstType
import org.easysql.util.anyToExpr

import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

sealed trait Expr[T <: SqlSingleConstType | Null](var alias: Option[String] = None) {
    def +[V <: T & SqlSingleConstType](value: V): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.ADD, const(value))

    def +[V <: T | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.ADD, expr)

    def +[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.ADD, SubQueryExpr(subQuery))

    def -[V <: T & SqlSingleConstType](value: V): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.SUB, const(value))

    def -[V <: T | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.SUB, expr)

    def -[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.SUB, SubQueryExpr(subQuery))

    def *[V <: T & SqlSingleConstType](value: V): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MUL, const(value))

    def *[V <: T | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MUL, expr)

    def *[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MUL, SubQueryExpr(subQuery))

    def /[V <: T & SqlSingleConstType](value: V): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.DIV, const(value))

    def /[V <: T | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.DIV, expr)

    def /[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.DIV, SubQueryExpr(subQuery))

    def %[V <: T & SqlSingleConstType](value: V): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MOD, const(value))

    def %[V <: T | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MOD, expr)

    def %[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr[T](this, SqlBinaryOperator.MOD, SubQueryExpr(subQuery))

    def ==[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, const(value))

    def ==[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, expr)

    def ==[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def ===[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, const(value))

    def ===[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, expr)

    def ===[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def equal(expr: Any): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.EQ, anyToExpr(expr))

    def <>[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.NE, const(value))

    def <>[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.NE, expr)

    def <>[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.NE, SubQueryExpr(subQuery))

    def >[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GT, const(value))

    def >[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GT, expr)

    def >[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GT, SubQueryExpr(subQuery))

    def >=[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GE, const(value))

    def >=[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GE, expr)

    def >=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.GE, SubQueryExpr(subQuery))

    def <[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LT, const(value))

    def <[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LT, expr)

    def <[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LT, SubQueryExpr(subQuery))

    def <=[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LE, const(value))

    def <=[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LE, expr)

    def <=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.LE, SubQueryExpr(subQuery))

    infix def &&(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.AND, query)

    infix def ||(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.OR, query)

    infix def ^(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr[Boolean](this, SqlBinaryOperator.XOR, query)

    infix def in[V <: T](list: List[V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        InListExpr(this, list)
    }

    infix def in[V <: T](list: (V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean] = {
        InListExpr(this, list.toList)
    }

    infix def notIn[V <: T](list: List[V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        InListExpr(this, list, true)
    }

    infix def notIn[V <: T](list: (V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean] = {
        InListExpr(this, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean] = InSubQueryExpr(this, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean] = InSubQueryExpr(this, subQuery, true)

    infix def between[V <: T](between: Tuple2[(V & SqlSingleConstType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlSingleConstType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2)
    }

    infix def notBetween[V <: T](between: Tuple2[(V & SqlSingleConstType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlSingleConstType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2, true)
    }

    def asc: OrderBy = OrderBy(this, SqlOrderByOption.ASC)

    def desc: OrderBy = OrderBy(this, SqlOrderByOption.DESC)

    infix def as(name: String)(using NonEmpty[name.type]): Expr[T] = {
        this.alias = Some(name)
        this
    }

    infix def unsafeAs(name: String): Expr[T] = {
        this.alias = Some(name)
        this
    }
}

extension[T <: String | Null] (e: Expr[T]) {
    infix def like(value: String | Expr[String] | Expr[String | Null]): BinaryExpr[Boolean] = {
        val query = value match {
            case s: String => const(s)
            case q: Expr[_] => q
        }
        BinaryExpr(e, SqlBinaryOperator.LIKE, query)
    }

    infix def notLike(value: String | Expr[String] | Expr[String | Null]): BinaryExpr[Boolean] = {
        val query = value match {
            case s: String => const(s)
            case q: Expr[_] => q
        }
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, query)
    }
}

extension[T <: SqlSingleConstType] (e: Expr[T | Null]) {
    @deprecated("请使用expr === null")
    def isNull: BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.IS, const(null))

    @deprecated("请使用expr <> null")
    def isNotNull: BinaryExpr[Boolean] = BinaryExpr[Boolean](e, SqlBinaryOperator.IS_NOT, const(null))
}

case class ConstExpr[T <: SqlSingleConstType | Null](value: T) extends Expr[T]()

case class BinaryExpr[T <: SqlSingleConstType | Null](left: Expr[_],
                                                      operator: SqlBinaryOperator,
                                                      right: Expr[_]) extends Expr[T]() {
    def thenIs[TV <: SqlSingleConstType | Null](thenValue: TV | Expr[TV] | SelectQuery[Tuple1[TV]]): CaseBranch[TV] = {
        CaseBranch(this, thenValue)
    }
}

case class ColumnExpr[T <: SqlSingleConstType | Null](column: String) extends Expr[T]()

case class TableColumnExpr[T <: SqlSingleConstType | Null](table: String,
                                                    column: String) extends Expr[T]() {
    def primaryKey: PrimaryKeyColumnExpr[T & SqlSingleConstType] = {
        PrimaryKeyColumnExpr(table, column)
    }

    def nullable: TableColumnExpr[T | Null] = {
        val copy: TableColumnExpr[T | Null] = this.copy()
        copy
    }

    override infix def as(name: String)(using NonEmpty[name.type]): Expr[T] = {
        val copy: TableColumnExpr[T] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T] = {
        val copy: TableColumnExpr[T] = this.copy()
        copy.alias = Some(name)
        copy
    }
}

extension [T <: Int | Long](t: TableColumnExpr[T]) {
    def incr: PrimaryKeyColumnExpr[T] = {
        PrimaryKeyColumnExpr(t.table, t.column, true)
    }
}

case class PrimaryKeyColumnExpr[T <: SqlSingleConstType](table: String,
                                                         column: String,
                                                         var isIncr: Boolean = false) extends Expr[T]() {
    override infix def as(name: String)(using NonEmpty[name.type]): Expr[T] = {
        val copy: PrimaryKeyColumnExpr[T] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T] = {
        val copy: PrimaryKeyColumnExpr[T] = this.copy()
        copy.alias = Some(name)
        copy
    }
}

case class SubQueryExpr[T <: SqlSingleConstType | Null](selectQuery: SelectQuery[Tuple1[T]]) extends Expr[T]()

case class NormalFunctionExpr[T <: SqlSingleConstType | Null](name: String, args: List[Expr[_]]) extends Expr[T]()

case class AggFunctionExpr[T <: SqlSingleConstType | Null](name: String,
                                                           args: List[Expr[_]],
                                                           distinct: Boolean = false,
                                                           attributes: Map[String, Expr[_]] = Map(),
                                                           orderBy: List[OrderBy] = List()) extends Expr[T]() {
    def over: OverExpr[T] = OverExpr(this)
}

case class CaseExpr[T <: SqlSingleConstType | Null](conditions: List[CaseBranch[T]],
                                                    var default: T | Expr[T] | SelectQuery[Tuple1[T]] | Null = null) extends Expr[T]() {
    infix def elseIs(value: T | Expr[T] | SelectQuery[Tuple1[T]] | Null): CaseExpr[T] = {
        if (value != null) {
            CaseExpr(this.conditions, value)
        } else {
            this
        }
    }
}

case class ListExpr[T <: SqlSingleConstType | Null](list: List[T | Expr[_] | SelectQuery[_]]) extends Expr[T]()

case class InListExpr[T <: SqlSingleConstType | Null](query: Expr[_],
                                                      list: List[T | Expr[_] | SelectQuery[_]],
                                                      isNot: Boolean = false) extends Expr[Boolean]()

case class InSubQueryExpr[T <: SqlSingleConstType | Null](query: Expr[T], subQuery: SelectQuery[_], isNot: Boolean = false) extends Expr[Boolean]()

case class CastExpr[T <: SqlSingleConstType | Null](query: Expr[_], castType: String) extends Expr[T]()

case class BetweenExpr[T <: SqlSingleConstType | Null](query: Expr[_],
                                                       start: T | Expr[_] | SelectQuery[_],
                                                       end: T | Expr[_] | SelectQuery[_],
                                                       isNot: Boolean = false) extends Expr[Boolean]()

case class AllColumnExpr(owner: Option[String] = None) extends Expr[Nothing]()

case class OverExpr[T <: SqlSingleConstType | Null](function: AggFunctionExpr[_],
                                                    partitionBy: ListBuffer[Expr[_]] = ListBuffer(),
                                                    orderBy: ListBuffer[OrderBy] = ListBuffer()) extends Expr[T]() {
    def partitionBy(query: Expr[_]*): OverExpr[T] = OverExpr(this.function, this.partitionBy.addAll(query), this.orderBy)

    def orderBy(order: OrderBy*): OverExpr[T] = OverExpr(this.function, this.partitionBy, this.orderBy.addAll(order))
}

case class SubQueryPredicateExpr[T <: SqlSingleConstType | Null](query: SelectQuery[_], predicate: SqlSubQueryPredicate) extends Expr[T]()

case class CaseBranch[T <: SqlSingleConstType | Null](query: Expr[_], thenValue: T | Expr[T] | SelectQuery[Tuple1[T]])

case class OrderBy(query: Expr[_], order: SqlOrderByOption)