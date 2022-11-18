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

trait ExprOperator[T <: SqlDataType] {
    extension (e: Expr[T]) {
        def ===(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.EQ, const(value))

        def ===(value: Option[T]): BinaryExpr[Boolean] = value match {
            case Some(v) => BinaryExpr(e, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(e, SqlBinaryOperator.EQ, const(null))
        }

        def ===(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.EQ, expr)

        def <>(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.NE, const(value))

        def <>(value: Option[T]): BinaryExpr[Boolean] = value match {
            case Some(v) => BinaryExpr(e, SqlBinaryOperator.NE, const(v))
            case None => BinaryExpr(e, SqlBinaryOperator.NE, const(null))
        }

        def <>(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.NE, expr)

        def >(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GT, const(value))

        def >(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GT, expr)

        def >=(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GE, const(value))

        def >=(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GE, expr)

        def <(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LT, const(value))

        def <(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LT, expr)

        def <=(value: T): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LE, const(value))

        def <=(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LE, expr)

        infix def in(list: List[T | Expr[T]]): Expr[Boolean] =
            if list.isEmpty then const(false) else InListExpr(e, list)

        infix def in(list: (T | Expr[T])*): Expr[Boolean] =
            InListExpr(e, list.toList)

        infix def notIn(list: List[T | Expr[T]]): Expr[Boolean] =
            if list.isEmpty then const(true) else InListExpr(e, list, true)

        infix def notIn(list: (T | Expr[T])*): Expr[Boolean] =
            InListExpr(e, list.toList, true)

        infix def in(subQuery: SelectQuery[Tuple1[T], _]): Expr[Boolean] = InSubQueryExpr(e, subQuery)

        infix def notIn(subQuery: SelectQuery[Tuple1[T], _]): Expr[Boolean] = InSubQueryExpr(e, subQuery, true)

        infix def between(start: T | Expr[T], end: T | Expr[T]): Expr[Boolean] =
            BetweenExpr(e, start, end)

        infix def notBetween(start: T | Expr[T], end: T | Expr[T]): Expr[Boolean] =
            BetweenExpr(e, start, end, true)

        def asc: OrderBy = OrderBy(e, SqlOrderByOption.ASC)

        def desc: OrderBy = OrderBy(e, SqlOrderByOption.DESC)

        inline infix def as(inline name: String)(using NonEmpty[name.type] =:= Any) = AliasExpr[T, name.type](e, name)

        infix def unsafeAs(name: String) = AliasExpr[T, name.type](e, name)
    }

    extension (v: T) {
        def ===(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.EQ, expr)

        def <>(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.NE, expr)

        def >(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.GT, expr)

        def >=(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.GE, expr)

        def <(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.LT, expr)

        def <=(expr: Expr[T]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.LE, expr)

        infix def in(list: List[Expr[T]]): Expr[Boolean] =
            if list.isEmpty then const(false) else InListExpr(const(v), list)

        infix def in(list: (Expr[T])*): Expr[Boolean] =
            InListExpr(const(v), list.toList)

        infix def notIn(list: List[Expr[T]]): Expr[Boolean] =
            if list.isEmpty then const(true) else InListExpr(const(v), list, true)

        infix def notIn(list: (Expr[T])*): Expr[Boolean] =
            InListExpr(const(v), list.toList, true)

        infix def between(start: Expr[T], end: Expr[T]): Expr[Boolean] =
            BetweenExpr(const(v), start, end)

        infix def notBetween(start: Expr[T], end: Expr[T]): Expr[Boolean] =
            BetweenExpr(const(v), start, end, true)

        inline infix def as(inline name: String)(using NonEmpty[name.type] =:= Any) = AliasExpr[T, name.type](const(v), name)

        infix def unsafeAs(name: String) = AliasExpr[T, name.type](const(v), name)
    }
}

sealed trait Expr[T <: SqlDataType] extends SelectItem[T] {
    def equal(expr: Any): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, anyToExpr(expr))
}

object Expr {
    given intOperator: ExprOperator[Int] with {
        extension [T <: Int | Long | Float | Double] (e: Expr[Int]) {
            def +(value: Int): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Long): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

            def -(value: Int): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Long): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

            def *(value: Int): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Long): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

            def /(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

            def %(value: Int): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Long): BinaryExpr[Int] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)
        }

        extension [T <: Int | Long | Float | Double] (v: Int) {
            def +(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.ADD, expr)

            def -(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.SUB, expr)

            def *(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.MUL, expr)

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.DIV, expr)

            def %(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.MOD, expr)
        }
    }

    given longOperator: ExprOperator[Long] with {
        extension [T <: Int | Long | Float | Double] (e: Expr[Long]) {
            def +(value: Int): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Long): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(expr: Expr[T]): BinaryExpr[NumberOperationType[Long, T]] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

            def -(value: Int): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Long): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(expr: Expr[T]): BinaryExpr[NumberOperationType[Long, T]] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

            def *(value: Int): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Long): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(expr: Expr[T]): BinaryExpr[NumberOperationType[Long, T]] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

            def /(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

            def %(value: Int): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Long): BinaryExpr[Long] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Float): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Double): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(expr: Expr[T]): BinaryExpr[NumberOperationType[Long, T]] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)
        }

        extension [T <: Int | Long | Float | Double] (v: Long) {
            def +(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.ADD, expr)

            def -(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.SUB, expr)

            def *(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.MUL, expr)

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.DIV, expr)

            def %(expr: Expr[T]): BinaryExpr[T] = BinaryExpr(const(v), SqlBinaryOperator.MOD, expr)
        }
    }

    given floatOperator: ExprOperator[Float] with {
        extension [T <: Int | Long | Float | Double] (e: Expr[Float]) {
            def +(value: Int): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Long): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Float): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(value: Double): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(expr: Expr[T]): BinaryExpr[NumberOperationType[Float, T]] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

            def -(value: Int): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Long): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Float): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(value: Double): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(expr: Expr[T]): BinaryExpr[NumberOperationType[Float, T]] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

            def *(value: Int): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Long): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Float): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(value: Double): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(expr: Expr[T]): BinaryExpr[NumberOperationType[Float, T]] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

            def /(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

            def %(value: Int): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Long): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Float): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(value: Double): BinaryExpr[Float] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(expr: Expr[T]): BinaryExpr[NumberOperationType[Float, T]] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)
        }

        extension [T <: Int | Long | Float | Double] (v: Float) {
            def +(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.ADD, expr)

            def -(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.SUB, expr)

            def *(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.MUL, expr)

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.DIV, expr)

            def %(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.MOD, expr)
        }
    }

    given doubleOperator: ExprOperator[Double] with {
        extension [T <: Int | Long | Float | Double] (e: Expr[Double]) {
            def +(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

            def -(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

            def *(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

            def /(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

            def %(value: T): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)
        }

        extension [T <: Int | Long | Float | Double] (v: Double) {
            def +(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.ADD, expr)

            def -(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.SUB, expr)

            def *(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.MUL, expr)

            def /(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.DIV, expr)

            def %(expr: Expr[T]): BinaryExpr[Double] = BinaryExpr(const(v), SqlBinaryOperator.MOD, expr)
        }
    }

    given decimalOperator: ExprOperator[BigDecimal] with {
        extension [T <: SqlNumberType] (e: Expr[BigDecimal]) {
            def +(value: T): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.ADD, const(value))

            def +(expr: Expr[T]): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.ADD, expr)

            def -(value: T): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.SUB, const(value))

            def -(expr: Expr[T]): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.SUB, expr)

            def *(value: T): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.MUL, const(value))

            def *(expr: Expr[T]): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.MUL, expr)

            def /(value: T): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.DIV, const(value))

            def /(expr: Expr[T]): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.DIV, expr)

            def %(value: T): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.MOD, const(value))

            def %(expr: Expr[T]): BinaryExpr[BigDecimal] = BinaryExpr(e, SqlBinaryOperator.MOD, expr)
        }

        extension [T <: SqlNumberType] (v: T) {
            def +(expr: Expr[BigDecimal]): BinaryExpr[BigDecimal] = BinaryExpr(const(v), SqlBinaryOperator.ADD, expr)

            def -(expr: Expr[BigDecimal]): BinaryExpr[BigDecimal] = BinaryExpr(const(v), SqlBinaryOperator.SUB, expr)

            def *(expr: Expr[BigDecimal]): BinaryExpr[BigDecimal] = BinaryExpr(const(v), SqlBinaryOperator.MUL, expr)

            def /(expr: Expr[BigDecimal]): BinaryExpr[BigDecimal] = BinaryExpr(const(v), SqlBinaryOperator.DIV, expr)

            def %(expr: Expr[BigDecimal]): BinaryExpr[BigDecimal] = BinaryExpr(const(v), SqlBinaryOperator.MOD, expr)
        }
    }

    given stringOperator: ExprOperator[String] with {
        extension (e: Expr[String]) {
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
    }

    given boolOperator: ExprOperator[Boolean] with {
        extension (e: Expr[Boolean]) {
            def &&(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.AND, query)

            def &&(v: Boolean): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.AND, const(v))

            def ||(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.OR, query)

            def ||(v: Boolean): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.OR, const(v))

            def ^(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.XOR, query)

            def ^(v: Boolean): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.XOR, const(v))

            def unary_! : NormalFunctionExpr[Boolean] = NormalFunctionExpr("NOT", List(e))
        }

        extension (v: Boolean) {
            def &&(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.AND, query)

            def ||(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.OR, query)

            def ^(query: Expr[Boolean]): BinaryExpr[Boolean] = BinaryExpr(const(v), SqlBinaryOperator.XOR, query)
        }
    }

    given dateOperator: ExprOperator[Date] with {
        extension (e: Expr[Date]) {
            def ===(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.EQ, const(s))

            def <>(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.NE, const(s))

            def >(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GT, const(s))

            def >=(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.GE, const(s))

            def <(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LT, const(s))

            def <=(s: String): BinaryExpr[Boolean] = BinaryExpr(e, SqlBinaryOperator.LE, const(s))

            infix def between(start: String, end: String): Expr[Boolean] =
                BetweenExpr(e, start, end)

            infix def notBetween(start: String, end: String): Expr[Boolean] =
                BetweenExpr(e, start, end, true)
        }
    }
}

case class ConstExpr[T <: SqlDataType](value: T) extends Expr[T]

case class BinaryExpr[T <: SqlDataType](
    left: Expr[_],
    operator: SqlBinaryOperator,
    right: Expr[_]
) extends Expr[T] {
    def thenIs[TV <: SqlDataType](thenValue: TV | Expr[TV] | SelectQuery[Tuple1[TV], _]): CaseBranch[TV] =
        CaseBranch(this, thenValue)
}

case class ColumnExpr[T <: SqlDataType](column: String) extends Expr[T]()

case class TableColumnExpr[T <: SqlDataType](
    table: String,
    column: String,
    schema: TableSchema[_]
) extends Expr[T]

case class PrimaryKeyColumnExpr[T <: SqlDataType](
    table: String,
    column: String,
    schema: TableSchema[_],
    isIncr: Boolean = false
) extends Expr[T]

case class SubQueryExpr[T <: SqlDataType](selectQuery: SelectQuery[Tuple1[T], _]) extends Expr[T]

case class NormalFunctionExpr[T <: SqlDataType](name: String, args: List[Expr[_]]) extends Expr[T]

case class AggFunctionExpr[T <: SqlDataType](
    name: String,
    args: List[Expr[_]],
    distinct: Boolean = false,
    attributes: Map[String, Expr[_]] = Map(),
    orderBy: List[OrderBy] = List()
) extends Expr[T] {
    def over: OverExpr[T] = OverExpr(this)
}

case class CaseExpr[T <: SqlDataType](
    conditions: List[CaseBranch[T]],
    default: T | Expr[T] | SelectQuery[Tuple1[T], _] = null
) extends Expr[T] {
    infix def elseIs(value: T | Expr[T] | SelectQuery[Tuple1[T], _]): CaseExpr[T] =
        if value != null then CaseExpr(this.conditions, value) else this
}

case class ListExpr[T <: SqlDataType](list: List[T | Expr[_] | SelectQuery[_, _]]) extends Expr[T]

case class InListExpr[T <: SqlDataType](
    query: Expr[_],
    list: List[T | Expr[_] | SelectQuery[_, _]],
    isNot: Boolean = false
) extends Expr[Boolean]

case class InSubQueryExpr[T <: SqlDataType](
    query: Expr[T], 
    subQuery: SelectQuery[_, _], 
    isNot: Boolean = false
) extends Expr[Boolean]

case class CastExpr[T <: SqlDataType](query: Expr[_], castType: String) extends Expr[T]

case class BetweenExpr[T <: SqlDataType](
    query: Expr[_],
    start: T | Expr[_] | SelectQuery[_, _],
    end: T | Expr[_] | SelectQuery[_, _],
    isNot: Boolean = false
) extends Expr[Boolean]

case class AllColumnExpr(owner: Option[String] = None) extends Expr[Nothing]()

case class OverExpr[T <: SqlDataType](
    function: AggFunctionExpr[_],
    partitionBy: ListBuffer[Expr[_]] = ListBuffer(),
    orderBy: ListBuffer[OrderBy] = ListBuffer()
) extends Expr[T] {
    def partitionBy(query: Expr[_]*): OverExpr[T] = OverExpr(this.function, this.partitionBy.addAll(query), this.orderBy)

    def orderBy(order: OrderBy*): OverExpr[T] = OverExpr(this.function, this.partitionBy, this.orderBy.addAll(order))
}

case class SubQueryPredicateExpr[T <: SqlDataType](query: SelectQuery[_, _], predicate: SqlSubQueryPredicate) extends Expr[T]

case class CaseBranch[T <: SqlDataType](query: Expr[_], thenValue: T | Expr[T] | SelectQuery[Tuple1[T], _])

case class OrderBy(query: Expr[_], order: SqlOrderByOption)