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
import scala.Tuple.Concat

sealed trait Expr[T <: SqlSingleConstType | Null, QuoteTables <: Tuple](var alias: Option[String] = None) {
    def +[V <: T & SqlSingleConstType](value: V): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.ADD, const(value))

    def +[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[T, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.ADD, expr)

    def +[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.ADD, SubQueryExpr(subQuery))

    def -[V <: T & SqlSingleConstType](value: V): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.SUB, const(value))

    def -[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[T, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.SUB, expr)

    def -[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.SUB, SubQueryExpr(subQuery))

    def *[V <: T & SqlSingleConstType](value: V): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.MUL, const(value))

    def *[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[T, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.MUL, expr)

    def *[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.MUL, SubQueryExpr(subQuery))

    def /[V <: T & SqlSingleConstType](value: V): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.DIV, const(value))

    def /[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[T, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.DIV, expr)

    def /[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.DIV, SubQueryExpr(subQuery))

    def %[V <: T & SqlSingleConstType](value: V): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.MOD, const(value))

    def %[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[T, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.MOD, expr)

    def %[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.MOD, SubQueryExpr(subQuery))

    def ==[V <: T](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.EQ, const(value))

    def ==[V <: T](value: Option[V]): BinaryExpr[Boolean, QuoteTables] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.EQ, const(null))
        }
    }

    def ==[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.EQ, expr)

    def ==[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def ===[V <: T](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.EQ, const(value))

    def ===[V <: T](value: Option[V]): BinaryExpr[Boolean, QuoteTables] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.EQ, const(null))
        }
    }

    def ===[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.EQ, expr)

    def ===[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def equal(expr: Any): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.EQ, anyToExpr(expr))

    def <>[V <: T](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.NE, const(value))

    def <>[V <: T](value: Option[V]): BinaryExpr[Boolean, QuoteTables] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.NE, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.NE, const(null))
        }
    }

    def <>[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.NE, expr)

    def <>[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.NE, SubQueryExpr(subQuery))

    def >[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.GT, const(value))

    def >[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.GT, expr)

    def >[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.GT, SubQueryExpr(subQuery))

    def >=[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.GE, const(value))

    def >=[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.GE, expr)

    def >=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.GE, SubQueryExpr(subQuery))

    def <[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.LT, const(value))

    def <[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.LT, expr)

    def <[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.LT, SubQueryExpr(subQuery))

    def <=[V <: T & SqlSingleConstType](value: V): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.LE, const(value))

    def <=[V <: T | Null, Tables <: Tuple](expr: Expr[V, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.LE, expr)

    def <=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean, QuoteTables] = BinaryExpr(this, SqlBinaryOperator.LE, SubQueryExpr(subQuery))

    def &&[Tables <: Tuple](query: Expr[_, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.AND, query)

    def ||[Tables <: Tuple](query: Expr[_, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.OR, query)

    def ^[Tables <: Tuple](query: Expr[_, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = BinaryExpr(this, SqlBinaryOperator.XOR, query)

    infix def in[V <: T](list: List[V | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean, EmptyTuple] = {
        if (list.isEmpty) {
            const(false)
        } else {
            InListExpr(this, list)
        }
    }

    infix def in[V <: T](list: (V | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean, EmptyTuple] = {
        InListExpr(this, list.toList)
    }

    infix def notIn[V <: T](list: List[V | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean, EmptyTuple] = {
        if (list.isEmpty) {
            const(true)
        } else {
            InListExpr(this, list, true)
        }
    }

    infix def notIn[V <: T](list: (V | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean, EmptyTuple] = {
        InListExpr(this, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean, EmptyTuple] = InSubQueryExpr(this, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean, EmptyTuple] = InSubQueryExpr(this, subQuery, true)

    infix def between[V <: T](between: ((V & SqlSingleConstType) | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlSingleConstType) | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])): Expr[Boolean, EmptyTuple] = {
        BetweenExpr(this, between._1, between._2)
    }

    infix def notBetween[V <: T](between: ((V & SqlSingleConstType) | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlSingleConstType) | Expr[V, _] | Expr[V | Null, _] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])): Expr[Boolean, EmptyTuple] = {
        BetweenExpr(this, between._1, between._2, true)
    }

    def asc: OrderBy[QuoteTables] = OrderBy(this, SqlOrderByOption.ASC)

    def desc: OrderBy[QuoteTables] = OrderBy(this, SqlOrderByOption.DESC)

    infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T, QuoteTables] = {
        this.alias = Some(name)
        this
    }

    infix def unsafeAs(name: String): Expr[T, QuoteTables] = {
        this.alias = Some(name)
        this
    }
}

extension[T <: String | Null, QuoteTables <: Tuple] (e: Expr[T, QuoteTables]) {
    infix def like(value: String): BinaryExpr[Boolean, QuoteTables] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, const(value))
    }

    infix def like[Tables <: Tuple](expr: Expr[String, Tables] | Expr[String | Null, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, expr)
    }

    infix def notLike(value: String): BinaryExpr[Boolean, QuoteTables] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, const(value))
    }

    infix def notLike[Tables <: Tuple](expr: Expr[String, Tables] | Expr[String | Null, Tables]): BinaryExpr[Boolean, Concat[QuoteTables, Tables]] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, expr)
    }
}

case class ConstExpr[T <: SqlSingleConstType | Null](value: T) extends Expr[T, EmptyTuple]()

case class BinaryExpr[T <: SqlSingleConstType | Null, QuoteTables <: Tuple](left: Expr[_, _],
                                                      operator: SqlBinaryOperator,
                                                      right: Expr[_, _]) extends Expr[T, QuoteTables]() {
    def thenIs[TV <: SqlSingleConstType | Null](thenValue: TV | Expr[TV, _] | SelectQuery[Tuple1[TV]]): CaseBranch[TV] = {
        CaseBranch(this, thenValue)
    }
}

case class ColumnExpr[T <: SqlSingleConstType | Null](column: String) extends Expr[T, EmptyTuple]()

case class TableColumnExpr[T <: SqlSingleConstType | Null, QuoteTable <: TableSchema](table: String,
                                                           column: String) extends Expr[T, Tuple1[QuoteTable]]() {
    def primaryKey: PrimaryKeyColumnExpr[T & SqlSingleConstType, QuoteTable] = {
        PrimaryKeyColumnExpr(table, column)
    }

    def nullable: TableColumnExpr[T | Null, QuoteTable] = {
        val copy: TableColumnExpr[T | Null, QuoteTable] = this.copy()
        copy
    }

    override infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T, Tuple1[QuoteTable]] = {
        val copy: TableColumnExpr[T, QuoteTable] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T, Tuple1[QuoteTable]] = {
        val copy: TableColumnExpr[T, QuoteTable] = this.copy()
        copy.alias = Some(name)
        copy
    }
}

extension [T <: Int | Long, QuoteTable <: TableSchema](t: TableColumnExpr[T, QuoteTable]) {
    def incr: PrimaryKeyColumnExpr[T, QuoteTable] = {
        PrimaryKeyColumnExpr(t.table, t.column, true)
    }
}

case class PrimaryKeyColumnExpr[T <: SqlSingleConstType, QuoteTable <: TableSchema](table: String,
                                                         column: String,
                                                         var isIncr: Boolean = false) extends Expr[T, Tuple1[QuoteTable]]() {
    override infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T, Tuple1[QuoteTable]] = {
        val copy: PrimaryKeyColumnExpr[T, QuoteTable] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T, Tuple1[QuoteTable]] = {
        val copy: PrimaryKeyColumnExpr[T, QuoteTable] = this.copy()
        copy.alias = Some(name)
        copy
    }
}

case class SubQueryExpr[T <: SqlSingleConstType | Null](selectQuery: SelectQuery[Tuple1[T]]) extends Expr[T, EmptyTuple]()

case class NormalFunctionExpr[T <: SqlSingleConstType | Null](name: String, args: List[Expr[_, _]]) extends Expr[T, EmptyTuple]()

case class AggFunctionExpr[T <: SqlSingleConstType | Null](name: String,
                                                           args: List[Expr[_, _]],
                                                           distinct: Boolean = false,
                                                           attributes: Map[String, Expr[_, _]] = Map(),
                                                           orderBy: List[OrderBy[_]] = List()) extends Expr[T, EmptyTuple]() {
    def over: OverExpr[T] = OverExpr(this)
}

case class CaseExpr[T <: SqlSingleConstType | Null](conditions: List[CaseBranch[T]],
                                                    var default: T | Expr[T, _] | SelectQuery[Tuple1[T]] | Null = null) extends Expr[T, EmptyTuple]() {
    infix def elseIs(value: T | Expr[T, _] | SelectQuery[Tuple1[T]] | Null): CaseExpr[T] = {
        if (value != null) {
            CaseExpr(this.conditions, value)
        } else {
            this
        }
    }
}

case class ListExpr[T <: SqlSingleConstType | Null](list: List[T | Expr[_, _] | SelectQuery[_]]) extends Expr[T, EmptyTuple]()

case class InListExpr[T <: SqlSingleConstType | Null](query: Expr[_, _],
                                                      list: List[T | Expr[_, _] | SelectQuery[_]],
                                                      isNot: Boolean = false) extends Expr[Boolean, EmptyTuple]()

case class InSubQueryExpr[T <: SqlSingleConstType | Null](query: Expr[T, _], subQuery: SelectQuery[_], isNot: Boolean = false) extends Expr[Boolean, EmptyTuple]()

case class CastExpr[T <: SqlSingleConstType | Null](query: Expr[_, _], castType: String) extends Expr[T, EmptyTuple]()

case class BetweenExpr[T <: SqlSingleConstType | Null](query: Expr[_, _],
                                                       start: T | Expr[_, _] | SelectQuery[_],
                                                       end: T | Expr[_, _] | SelectQuery[_],
                                                       isNot: Boolean = false) extends Expr[Boolean, EmptyTuple]()

case class AllColumnExpr(owner: Option[String] = None) extends Expr[Nothing, EmptyTuple]()

case class OverExpr[T <: SqlSingleConstType | Null](function: AggFunctionExpr[_],
                                                    partitionBy: ListBuffer[Expr[_, _]] = ListBuffer(),
                                                    orderBy: ListBuffer[OrderBy[_]] = ListBuffer()) extends Expr[T, EmptyTuple]() {
    def partitionBy(query: Expr[_, _]*): OverExpr[T] = OverExpr(this.function, this.partitionBy.addAll(query), this.orderBy)

    def orderBy(order: OrderBy[_]*): OverExpr[T] = OverExpr(this.function, this.partitionBy, this.orderBy.addAll(order))
}

case class SubQueryPredicateExpr[T <: SqlSingleConstType | Null](query: SelectQuery[_], predicate: SqlSubQueryPredicate) extends Expr[T, EmptyTuple]()

case class CaseBranch[T <: SqlSingleConstType | Null](query: Expr[_, _], thenValue: T | Expr[T, _] | SelectQuery[Tuple1[T]])

case class OrderBy[QuoteTables <: Tuple](query: Expr[_, QuoteTables], order: SqlOrderByOption)