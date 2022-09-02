package org.easysql.macros

import org.easysql.dsl.TableSchema

import scala.quoted.{Expr, Quotes, Type}
import scala.collection.mutable.*

def insertMacroImpl[T <: Product](using q: Quotes, tpe: Type[T]): Expr[(String, List[(String, T => Any)])] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    var tableName = sym.name
    val insertFieldExprs = ListBuffer[Expr[String]]()
    val insertFunctionExprs = ListBuffer[Expr[T => Any]]()

    if (sym.annotations.size > 0) {
        val annotation = sym.annotations(1)
        annotation match {
            case Apply(Select(New(TypeIdent(name)), _), args) =>
                if (name == "Table") {
                    args match {
                        case Literal(v) :: Nil => tableName = v.value.toString()
                        case _ =>
                    }
                }

            case _ =>
        }
    }

    val fields = sym.declaredFields
    fields.foreach { field =>
        if (field.annotations.size > 0) {
            val annotation = field.annotations.head
            annotation match {
                case Apply(Select(New(TypeIdent(name)), _), args) =>
                    if (name == "PrimaryKey" || name == "Column") {
                        val insertName = args match {
                            case Literal(v) :: Nil => v.value.toString()
                            case _ => field.name
                        }

                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[T]), _ => TypeRepr.of[Any])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[T].asTerm
                            Select.unique(x, field.name)
                        }
                        val lambda = Lambda(field, mtpe, rhsFn).asExprOf[T => Any]
                        insertFieldExprs.addOne(Expr.apply(insertName))
                        insertFunctionExprs.addOne(lambda)
                    }
                case _ =>
            }
        }
    }

    if (insertFieldExprs.size == 0) {
        report.error(s"实体类${sym.name}中没有用来插入数据的字段")
    }

    val insertFields = Expr.ofList(insertFieldExprs.toList)
    val insertFunctions = Expr.ofList(insertFunctionExprs.toList)
    val tableNameExpr = Expr(tableName)

    '{ 
        val insertList = $insertFields.zip($insertFunctions)
        $tableNameExpr -> insertList
     }
}