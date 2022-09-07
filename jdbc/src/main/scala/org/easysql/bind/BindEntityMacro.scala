package org.easysql.bind

import org.easysql.query.select.Query
import org.easysql.ast.SqlDataType
import org.easysql.dsl.*

import scala.compiletime.*

inline def bindEntityMacro[T]: Map[String, Any] => T = ${ bindEntityMacroImpl[T] }

inline def bindQueryMacro[T](inline nextIndex: Int): (Int, Array[Any] => T) = ${ bindQueryMacroImpl[T]('nextIndex) }

inline def bindSingleton[T](nextIndex: Int): (Int, Array[Any] => T) = {
    inline erasedValue[T] match {
        case _: SqlDataType =>
            nextIndex + 1 -> { (data: Array[Any]) => data(nextIndex).asInstanceOf[T] }

        case _ => bindQueryMacro[T](nextIndex)
    }
}

inline def bindQuery[T]: Array[Any] => FlatType[FlatType[T, SqlDataType | Null, Expr],Product,TableSchema] = { (data: Array[Any]) =>
    val bind = inline erasedValue[FlatType[FlatType[T, SqlDataType | Null, Expr], Product, TableSchema]] match {
        case _: Tuple1[t1] => bindSingleton[t1](0)._2.apply(data)
        case _: Tuple2[t1, t2] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            Tuple2(b1._2(data), b2._2(data))
        }
        case _: Tuple3[t1, t2, t3] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            Tuple3(b1._2(data), b2._2(data), b3._2(data))
        }
        case _: Tuple4[t1, t2, t3, t4] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            Tuple4(b1._2(data), b2._2(data), b3._2(data), b4._2(data))
        }
        case _: Tuple5[t1, t2, t3, t4, t5] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            Tuple5(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data))
        }
        case _: Tuple6[t1, t2, t3, t4, t5, t6] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            Tuple6(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data))
        }
        case _: Tuple7[t1, t2, t3, t4, t5, t6, t7] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            Tuple7(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data))
        }
        case _: Tuple8[t1, t2, t3, t4, t5, t6, t7, t8] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            Tuple8(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data))
        }
        case _: Tuple9[t1, t2, t3, t4, t5, t6, t7, t8, t9] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            Tuple9(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data))
        }
        case _: Tuple10[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            Tuple10(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data))
        }
        case _: Tuple11[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            Tuple11(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data))
        }
        case _: Tuple12[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            Tuple12(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data))
        }
        case _: Tuple13[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            Tuple13(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data))
        }
        case _: Tuple14[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            Tuple14(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data))
        }
        case _: Tuple15[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            Tuple15(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data))
        }
        case _: Tuple16[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            Tuple16(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data))
        }
        case _: Tuple17[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            Tuple17(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data))
        }
        case _: Tuple18[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            val b18 = bindSingleton[t18](b17._1)
            Tuple18(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data), b18._2(data))
        }
        case _: Tuple19[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            val b18 = bindSingleton[t18](b17._1)
            val b19 = bindSingleton[t19](b18._1)
            Tuple19(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data), b18._2(data), b19._2(data))
        }
        case _: Tuple20[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            val b18 = bindSingleton[t18](b17._1)
            val b19 = bindSingleton[t19](b18._1)
            val b20 = bindSingleton[t20](b19._1)
            Tuple20(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data), b18._2(data), b19._2(data), b20._2(data))
        }
        case _: Tuple21[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            val b18 = bindSingleton[t18](b17._1)
            val b19 = bindSingleton[t19](b18._1)
            val b20 = bindSingleton[t20](b19._1)
            val b21 = bindSingleton[t21](b20._1)
            Tuple21(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data), b18._2(data), b19._2(data), b20._2(data), b21._2(data))
        }
        case _: Tuple22[t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22] => {
            val b1 = bindSingleton[t1](0)
            val b2 = bindSingleton[t2](b1._1)
            val b3 = bindSingleton[t3](b2._1)
            val b4 = bindSingleton[t4](b3._1)
            val b5 = bindSingleton[t5](b4._1)
            val b6 = bindSingleton[t6](b5._1)
            val b7 = bindSingleton[t7](b6._1)
            val b8 = bindSingleton[t8](b7._1)
            val b9 = bindSingleton[t9](b8._1)
            val b10 = bindSingleton[t10](b9._1)
            val b11 = bindSingleton[t11](b10._1)
            val b12 = bindSingleton[t12](b11._1)
            val b13 = bindSingleton[t13](b12._1)
            val b14 = bindSingleton[t14](b13._1)
            val b15 = bindSingleton[t15](b14._1)
            val b16 = bindSingleton[t16](b15._1)
            val b17 = bindSingleton[t17](b16._1)
            val b18 = bindSingleton[t18](b17._1)
            val b19 = bindSingleton[t19](b18._1)
            val b20 = bindSingleton[t20](b19._1)
            val b21 = bindSingleton[t21](b20._1)
            val b22 = bindSingleton[t22](b21._1)
            Tuple22(b1._2(data), b2._2(data), b3._2(data), b4._2(data), b5._2(data), b6._2(data), b7._2(data), b8._2(data), b9._2(data), b10._2(data), b11._2(data), b12._2(data), b13._2(data), b14._2(data), b15._2(data), b16._2(data), b17._2(data), b18._2(data), b19._2(data), b20._2(data), b21._2(data), b22._2(data))
        }
        case _ => bindSingleton[FlatType[FlatType[T, SqlDataType | Null, Expr], Product, TableSchema]](0)._2.apply(data)
    }

    bind.asInstanceOf[FlatType[FlatType[T, SqlDataType | Null, Expr], Product, TableSchema]]
}