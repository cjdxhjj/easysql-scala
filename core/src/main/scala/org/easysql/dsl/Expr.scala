package org.easysql.dsl

import org.easysql.ast.expr.{SqlBinaryOperator, SqlExpr, SqlSubQueryPredicate}
import org.easysql.visitor.*
import org.easysql.ast.order.SqlOrderByOption
import org.easysql.dsl.const
import org.easysql.query.select.SelectQuery
import org.easysql.ast.{SqlDataType, SqlNumberType}
import org.easysql.database.TableEntity
import org.easysql.util.anyToExpr

import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.Tuple.Concat

sealed trait Expr[T <: SqlDataType | Null](var alias: Option[String] = None) {
    def +[V <: T & SqlNumberType](value: V): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.ADD, const(value))

    def +[V <: (T & SqlNumberType) | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.ADD, expr)

    def +[V <: (T & SqlNumberType) | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.ADD, SubQueryExpr(subQuery))

    def -[V <: T & SqlNumberType](value: V): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.SUB, const(value))

    def -[V <: (T & SqlNumberType) | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.SUB, expr)

    def -[V <: (T & SqlNumberType) | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.SUB, SubQueryExpr(subQuery))

    def *[V <: T & SqlNumberType](value: V): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MUL, const(value))

    def *[V <: (T & SqlNumberType) | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MUL, expr)

    def *[V <: (T & SqlNumberType) | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MUL, SubQueryExpr(subQuery))

    def /[V <: T & SqlNumberType](value: V): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.DIV, const(value))

    def /[V <: (T & SqlNumberType) | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.DIV, expr)

    def /[V <: (T & SqlNumberType) | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.DIV, SubQueryExpr(subQuery))

    def %[V <: T & SqlNumberType](value: V): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MOD, const(value))

    def %[V <: (T & SqlNumberType) | Null](expr: Expr[V]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MOD, expr)

    def %[V <: (T & SqlNumberType) | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[T] = BinaryExpr(this, SqlBinaryOperator.MOD, SubQueryExpr(subQuery))

    def ==[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, const(value))

    def ==[V <: T](value: Option[V]): BinaryExpr[Boolean] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.EQ, const(null))
        }
    }

    def ==[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, expr)

    def ==[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def ===[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, const(value))

    def ===[V <: T](value: Option[V]): BinaryExpr[Boolean] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.EQ, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.EQ, const(null))
        }
    }

    def ===[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, expr)

    def ===[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, SubQueryExpr(subQuery))

    def equal(expr: Any): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.EQ, anyToExpr(expr))

    def <>[V <: T](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, const(value))

    def <>[V <: T](value: Option[V]): BinaryExpr[Boolean] = {
        value match {
            case Some(v) => BinaryExpr(this, SqlBinaryOperator.NE, const(v))
            case None => BinaryExpr(this, SqlBinaryOperator.NE, const(null))
        }
    }

    def <>[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, expr)

    def <>[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.NE, SubQueryExpr(subQuery))

    def >[V <: T & SqlDataType](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, const(value))

    def >[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, expr)

    def >[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GT, SubQueryExpr(subQuery))

    def >=[V <: T & SqlDataType](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, const(value))

    def >=[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, expr)

    def >=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.GE, SubQueryExpr(subQuery))

    def <[V <: T & SqlDataType](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, const(value))

    def <[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, expr)

    def <[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LT, SubQueryExpr(subQuery))

    def <=[V <: T & SqlDataType](value: V): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, const(value))

    def <=[V <: T | Null](expr: Expr[V]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, expr)

    def <=[V <: T | Null](subQuery: SelectQuery[Tuple1[V]]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.LE, SubQueryExpr(subQuery))

    def &&(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.AND, query)

    def ||(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.OR, query)

    def ^(query: Expr[_]): BinaryExpr[Boolean] = BinaryExpr(this, SqlBinaryOperator.XOR, query)

    def unary_! : NormalFunctionExpr[Boolean] = NormalFunctionExpr("NOT", List(this))

    infix def in[V <: T](list: List[V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        if (list.isEmpty) {
            const(false)
        } else {
            InListExpr(this, list)
        }
    }

    infix def in[V <: T](list: (V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean] = {
        InListExpr(this, list.toList)
    }

    infix def notIn[V <: T](list: List[V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]]]): Expr[Boolean] = {
        if (list.isEmpty) {
            const(true)
        } else {
            InListExpr(this, list, true)
        }
    }

    infix def notIn[V <: T](list: (V | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])*): Expr[Boolean] = {
        InListExpr(this, list.toList, true)
    }

    infix def in(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean] = InSubQueryExpr(this, subQuery)

    infix def notIn(subQuery: SelectQuery[Tuple1[T]] | SelectQuery[Tuple1[T | Null]]): Expr[Boolean] = InSubQueryExpr(this, subQuery, true)

    infix def between[V <: T](between: ((V & SqlDataType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlDataType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2)
    }

    infix def notBetween[V <: T](between: ((V & SqlDataType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]], (V & SqlDataType) | Expr[V] | Expr[V | Null] | SelectQuery[Tuple1[V]] | SelectQuery[Tuple1[V | Null]])): Expr[Boolean] = {
        BetweenExpr(this, between._1, between._2, true)
    }

    def asc: OrderBy = OrderBy(this, SqlOrderByOption.ASC)

    def desc: OrderBy = OrderBy(this, SqlOrderByOption.DESC)

    infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T] = {
        this.alias = Some(name)
        this
    }

    infix def unsafeAs(name: String): Expr[T] = {
        this.alias = Some(name)
        this
    }
}

extension[T <: String | Null] (e: Expr[T]) {
    infix def like(value: String): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, const(value))
    }

    infix def like(expr: Expr[String] | Expr[String | Null]): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.LIKE, expr)
    }

    infix def notLike(value: String): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, const(value))
    }

    infix def notLike(expr: Expr[String] | Expr[String | Null]): BinaryExpr[Boolean] = {
        BinaryExpr(e, SqlBinaryOperator.NOT_LIKE, expr)
    }
}

case class ConstExpr[T <: SqlDataType | Null](value: T) extends Expr[T]()

case class BinaryExpr[T <: SqlDataType | Null](left: Expr[_],
                                               operator: SqlBinaryOperator,
                                               right: Expr[_]) extends Expr[T]() {
    def thenIs[TV <: SqlDataType | Null](thenValue: TV | Expr[TV] | SelectQuery[Tuple1[TV]]): CaseBranch[TV] = {
        CaseBranch(this, thenValue)
    }
}

case class ColumnExpr[T <: SqlDataType | Null](column: String) extends Expr[T]()

case class TableColumnExpr[T <: SqlDataType, E <: TableEntity[_]](table: String,
                                                                  column: String,
                                                                  schema: TableSchema[E],
                                                                  bind: Option[E => T] = None) extends Expr[T]() {
    def primaryKey: PrimaryKeyColumnExpr[T & SqlDataType, E] = {
        PrimaryKeyColumnExpr(table, column, schema)
    }

    def nullable: NullableColumnExpr[T, E] = {
        val copy: NullableColumnExpr[T, E] = NullableColumnExpr(table, column, schema, bind = None)
        copy
    }

    override infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T] = {
        val copy: TableColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T] = {
        val copy: TableColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    inline def bind(inline f: E => T): TableColumnExpr[T, E] = {
        import scala.compiletime.codeOf
        import scala.language.unsafeNulls
        
        val fieldName = codeOf(f).split("\\.").last.split("\n").head.trim
        schema.$bind.put(fieldName, column)
        val col = this.copy(bind = Some(f))
        schema.$columns.append(col)
        col
    }
}

extension[T <: Int | Long, E <: TableEntity[_]] (t: TableColumnExpr[T, E]) {
    def incr: PrimaryKeyColumnExpr[T, E] = {
        PrimaryKeyColumnExpr(t.table, t.column, t.schema, true)
    }
}

case class NullableColumnExpr[T <: SqlDataType, E <: TableEntity[_]](table: String,
                                                                            column: String,
                                                                            schema: TableSchema[E],
                                                                            bind: Option[E => Option[T]] = None) extends Expr[T | Null]() {
    override infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T | Null] = {
        val copy: NullableColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T | Null] = {
        val copy: NullableColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    inline def bind(inline f: E => Option[T]): NullableColumnExpr[T, E] = {
        import scala.compiletime.codeOf
        import scala.language.unsafeNulls
        
        val fieldName = codeOf(f).split("\\.").last.split("\n").head.trim
        schema.$bind.put(fieldName, column)
        val col = this.copy(bind = Some(f))
        schema.$columns.append(col)
        col
    }
}

case class PrimaryKeyColumnExpr[T <: SqlDataType, E <: TableEntity[_]](table: String,
                                                                       column: String,
                                                                       schema: TableSchema[E],
                                                                       var isIncr: Boolean = false,
                                                                       bind: Option[E => T] = None) extends Expr[T]() {
    override infix def as(name: String)(using NonEmpty[name.type] =:= Any): Expr[T] = {
        val copy: PrimaryKeyColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    override infix def unsafeAs(name: String): Expr[T] = {
        val copy: PrimaryKeyColumnExpr[T, E] = this.copy()
        copy.alias = Some(name)
        copy
    }

    inline def bind(inline f: E => T): PrimaryKeyColumnExpr[T, E] = {
        import scala.compiletime.codeOf
        import scala.language.unsafeNulls

        val fieldName = codeOf(f).split("\\.").last.split("\n").head.trim
        schema.$bind.put(fieldName, column)
        val col = this.copy(bind = Some(f))
        schema.$pkCols.append(col)
        col
    }
}

case class SubQueryExpr[T <: SqlDataType | Null](selectQuery: SelectQuery[Tuple1[T]]) extends Expr[T]()

case class NormalFunctionExpr[T <: SqlDataType | Null](name: String, args: List[Expr[_]]) extends Expr[T]()

case class AggFunctionExpr[T <: SqlDataType | Null](name: String,
                                                    args: List[Expr[_]],
                                                    distinct: Boolean = false,
                                                    attributes: Map[String, Expr[_]] = Map(),
                                                    orderBy: List[OrderBy] = List()) extends Expr[T]() {
    def over: OverExpr[T] = OverExpr(this)
}

case class CaseExpr[T <: SqlDataType | Null](conditions: List[CaseBranch[T]],
                                             var default: T | Expr[T] | SelectQuery[Tuple1[T]] | Null = null) extends Expr[T]() {
    infix def elseIs(value: T | Expr[T] | SelectQuery[Tuple1[T]] | Null): CaseExpr[T] = {
        if (value != null) {
            CaseExpr(this.conditions, value)
        } else {
            this
        }
    }
}

case class ListExpr[T <: SqlDataType | Null](list: List[T | Expr[_] | SelectQuery[_]]) extends Expr[T]()

case class InListExpr[T <: SqlDataType | Null](query: Expr[_],
                                               list: List[T | Expr[_] | SelectQuery[_]],
                                               isNot: Boolean = false) extends Expr[Boolean]()

case class InSubQueryExpr[T <: SqlDataType | Null](query: Expr[T], subQuery: SelectQuery[_], isNot: Boolean = false) extends Expr[Boolean]()

case class CastExpr[T <: SqlDataType | Null](query: Expr[_], castType: String) extends Expr[T]()

case class BetweenExpr[T <: SqlDataType | Null](query: Expr[_],
                                                start: T | Expr[_] | SelectQuery[_],
                                                end: T | Expr[_] | SelectQuery[_],
                                                isNot: Boolean = false) extends Expr[Boolean]()

case class AllColumnExpr(owner: Option[String] = None) extends Expr[Nothing]()

case class OverExpr[T <: SqlDataType | Null](function: AggFunctionExpr[_],
                                             partitionBy: ListBuffer[Expr[_]] = ListBuffer(),
                                             orderBy: ListBuffer[OrderBy] = ListBuffer()) extends Expr[T]() {
    def partitionBy(query: Expr[_]*): OverExpr[T] = OverExpr(this.function, this.partitionBy.addAll(query), this.orderBy)

    def orderBy(order: OrderBy*): OverExpr[T] = OverExpr(this.function, this.partitionBy, this.orderBy.addAll(order))
}

case class SubQueryPredicateExpr[T <: SqlDataType | Null](query: SelectQuery[_], predicate: SqlSubQueryPredicate) extends Expr[T]()

case class CaseBranch[T <: SqlDataType | Null](query: Expr[_], thenValue: T | Expr[T] | SelectQuery[Tuple1[T]])

case class OrderBy(query: Expr[_], order: SqlOrderByOption)