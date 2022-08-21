package org.easysql.dsl

import org.easysql.ast.SqlDataType
import org.easysql.database.TableEntity
import org.easysql.query.select.{SelectQuery, ValuesSelect, Select}

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

type QueryType[T <: Tuple | Expr[_] | TableSchema[_]] <: Tuple = T match {
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

type SelectType[T <: Tuple] = T match {
    case Tuple1[t1] => Select[Tuple1[t1]] {val _1: Expr[t1]}
    case Tuple2[t1, t2] => Select[Tuple2[t1, t2]] {val _1: Expr[t1]; val _2: Expr[t2]}
    case Tuple3[t1, t2, t3] => Select[Tuple3[t1, t2, t3]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]}
    case Tuple4[t1, t2, t3, t4] => Select[Tuple4[t1, t2, t3, t4]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]}
    case Tuple5[t1, t2, t3, t4, t5] => Select[Tuple5[t1, t2, t3, t4, t5]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]}
    case Tuple6[t1, t2, t3, t4, t5, t6] => Select[Tuple6[t1, t2, t3, t4, t5, t6]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]}
    case Tuple7[t1, t2, t3, t4, t5, t6, t7] => Select[Tuple7[t1, t2, t3, t4, t5, t6, t7]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]}
    case Tuple8[t1, t2, t3, t4, t5, t6, t7, t8] => Select[Tuple8[t1, t2, t3, t4, t5, t6, t7, t8]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]}
    case Tuple9[t1, t2, t3, t4, t5, t6, t7, t8, t9] => Select[Tuple9[t1, t2, t3, t4, t5, t6, t7, t8, t9]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]}
    case Tuple10[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10] => Select[Tuple10[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]}
    case Tuple11[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11] => Select[Tuple11[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]}
    case Tuple12[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12] => Select[Tuple12[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]}
    case Tuple13[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13] => Select[Tuple13[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]}
    case Tuple14[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14] => Select[Tuple14[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]}
    case Tuple15[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15] => Select[Tuple15[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]}
    case Tuple16[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16] => Select[Tuple16[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]}
    case Tuple17[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17] => Select[Tuple17[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]}
    case Tuple18[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18] => Select[Tuple18[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]; val _18: Expr[t18]}
    case Tuple19[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19] => Select[Tuple19[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]; val _18: Expr[t18]; val _19: Expr[t19]}
    case Tuple20[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20] => Select[Tuple20[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]; val _18: Expr[t18]; val _19: Expr[t19]; val _20: Expr[t20]}
    case Tuple21[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21] => Select[Tuple21[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]; val _18: Expr[t18]; val _19: Expr[t19]; val _20: Expr[t20]; val _21: Expr[t21]}
    case Tuple22[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22] => Select[Tuple22[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22]] {val _1: Expr[t1]; val _2: Expr[t2]; val _3: Expr[t3]; val _4: Expr[t4]; val _5: Expr[t5]; val _6: Expr[t6]; val _7: Expr[t7]; val _8: Expr[t8]; val _9: Expr[t9]; val _10: Expr[t10]; val _11: Expr[t11]; val _12: Expr[t12]; val _13: Expr[t13]; val _14: Expr[t14]; val _15: Expr[t15]; val _16: Expr[t16]; val _17: Expr[t17]; val _18: Expr[t18]; val _19: Expr[t19]; val _20: Expr[t20]; val _21: Expr[t21]; val _22: Expr[t22]}
}