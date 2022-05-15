package org.easysql.dsl

import org.easysql.ast.SqlSingleConstType
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

given queryToExpr[T <: SqlSingleConstType | Null]: Conversion[SelectQuery[Tuple1[T]], SubQueryExpr[T]] = SubQueryExpr(_)

type InverseMap[X <: Tuple, F[_ <: SqlSingleConstType | Null]] <: Tuple = X match {
    case F[x] *: t => x *: InverseMap[t, F]
    case EmptyTuple => EmptyTuple
}

type RecursiveInverseMap[X <: Tuple, F[_ <: SqlSingleConstType | Null]] <: Tuple = X match {
    case x *: t => x match {
        case Tuple => Tuple.Concat[RecursiveInverseMap[x, F], RecursiveInverseMap[t, F]]
        case F[y] => y *: RecursiveInverseMap[t, F]
    }
    case EmptyTuple => EmptyTuple
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

type MatchTypeLeft[L, R] <: Boolean = L match {
    case R => true
    case R | Null => true
    case _ => false
}

type MatchTypeRight[L, R] <: Boolean = R match {
    case L => true
    case L | Null => true
    case _ => false
}