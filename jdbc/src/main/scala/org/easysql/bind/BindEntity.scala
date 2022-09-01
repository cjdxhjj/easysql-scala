package org.easysql.bind

import org.easysql.database.TableEntity
import org.easysql.dsl.TableSchema

import java.util.Date
import scala.reflect.ClassTag

//def bindData[T <: TableEntity[_]](data: Map[String, Any])(using t: TableSchema[T], ct: ClassTag[T]): T = {
//    import scala.language.unsafeNulls
//
//    val clazz = ct.runtimeClass
//    val ctor = clazz.getConstructors.head
//    val params = ctor.getParameters
//    val paramValues = params.map { p =>
//        val pName = p.getName
//        val pType = p.getType.getSimpleName
//        val value = pType match {
//            case "int" =>
//                t.$bind.get(pName).map(data.getOrElse(_, 0)).getOrElse(0)
//            case "long" =>
//                t.$bind.get(pName).map(data.getOrElse(_, 0l)).getOrElse(0l)
//            case "double" =>
//                t.$bind.get(pName).map(data.getOrElse(_, 0d)).getOrElse(0d)
//            case "float" =>
//                t.$bind.get(pName).map(data.getOrElse(_, 0f)).getOrElse(0f)
//            case "boolean" =>
//                t.$bind.get(pName).map(data.getOrElse(_, false)).getOrElse(false)
//            case "String" =>
//                t.$bind.get(pName).map(data.getOrElse(_, "")).getOrElse("")
//            case "Option" =>
//                if t.$bind.contains(pName) then {
//                    val colName = t.$bind(pName)
//                    if data.contains(colName) then Some(data(colName)) else None
//                } else None
//            case "Date" =>
//                t.$bind.get(pName).map(data.getOrElse(_, Date())).getOrElse(Date())
//            case "BigDecimal" =>
//                t.$bind.get(pName).map(data.getOrElse(_, BigDecimal(0))).getOrElse(BigDecimal(0))
//            case _ => null
//        }
//        value
//    }
//
//    val newInstance = ctor.newInstance(paramValues: _*)
//    newInstance.asInstanceOf[T]
//}
