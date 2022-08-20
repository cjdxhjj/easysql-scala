package org.easysql.dsl

import org.easysql.ast.SqlDataType
import org.easysql.database.TableEntity
import org.easysql.query.select.{SelectQuery, ValuesSelect}

import scala.compiletime.ops.any.*
import java.util.Date

given stringToExpr: Conversion[String, ConstExpr[String]] = ConstExpr[String](_)

given intToExpr: Conversion[Int, ConstExpr[Int]] = ConstExpr[Int](_)

given longToExpr: Conversion[Long, ConstExpr[Long]] = ConstExpr[Long](_)

given doubleToExpr: Conversion[Double, ConstExpr[Double]] = ConstExpr[Double](_)

given floatToExpr: Conversion[Float, ConstExpr[Float]] = ConstExpr[Float](_)

given boolToExpr: Conversion[Boolean, ConstExpr[Boolean]] = ConstExpr[Boolean](_)

given dateToExpr: Conversion[Date, ConstExpr[Date]] = ConstExpr[Date](_)

given decimalToExpr: Conversion[BigDecimal, ConstExpr[BigDecimal]] = ConstExpr[BigDecimal](_)

given queryToExpr[T <: SqlDataType | Null]: Conversion[SelectQuery[Tuple1[T]], SubQueryExpr[T]] = SubQueryExpr(_)

type InverseMap[X <: Tuple] <: Tuple = X match {
    case Expr[x] *: t => x *: InverseMap[t]
    case EmptyTuple => EmptyTuple
}

type RecursiveInverseMap[X <: Tuple] <: Tuple = X match {
    case x *: t => x match {
        case Tuple => Tuple.Concat[RecursiveInverseMap[x], RecursiveInverseMap[t]]
        case Expr[y] => y *: RecursiveInverseMap[t]
    }
    case EmptyTuple => EmptyTuple
}

type QueryType[T <: Tuple | Expr[_] | TableSchema] <: Tuple = T match {
    case h *: t => h *: t
    case _ => Tuple1[T]
}

type MapUnionNull[T <: Tuple] <: Tuple = T match {
    case h *: t => (h | Null) *: MapUnionNull[t]
    case EmptyTuple => EmptyTuple
}

type PK[T <: TableEntity[_]] = T match {
    case TableEntity[t] => t
}

type Union[X <: Tuple, Y <: Tuple] <: Tuple = (X, Y) match {
    case (a *: at, b *: bt) => UnionTo[a, b] *: Union[at, bt]
    case (EmptyTuple, EmptyTuple) => EmptyTuple
}

type UnionTo[A, B] = A match {
    case B => B
    case _ => B match {
        case A => A
    }
}

type NonEmpty[T <: String] = T == "" match {
    case false => Any
    case true => Nothing
}