package org.easysql.macros

import org.easysql.dsl.TableSchema
import org.easysql.ast.SqlDataType

import scala.quoted.{Expr, Quotes, Type}
import scala.collection.mutable.*
import scala.annotation.experimental

def insertMacroImpl[T <: Product](using q: Quotes, tpe: Type[T]): Expr[(String, List[(String, (T => Any) | (() => Any))])] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    var tableName = sym.name
    val insertFieldExprs = ListBuffer[Expr[String]]()
    val insertFunctionExprs = ListBuffer[Expr[(T => Any) | (() => Any)]]()

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
                            case Literal(v) :: _ => v.value.toString()
                            case _ => field.name
                        }

                        def createLambda = {
                            val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[T]), _ => TypeRepr.of[Any])
                            def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                                val x = paramRefs.head.asExprOf[T].asTerm
                                Select.unique(x, field.name)
                            }
                            Lambda(field, mtpe, rhsFn).asExprOf[T => Any]
                        }

                        def createGeneratorLambda(s: Statement) = {
                            val mtpe = MethodType(Nil)(_ => Nil, _ => TypeRepr.of[Any])
                            def rhsFn(sym: Symbol, paramRefs: List[Tree]): Tree = s match {
                                case DefDef(_, _, _, t) => t.get
                            }
                            Lambda(field, mtpe, rhsFn).asExprOf[() => Any]
                        }

                        val lambda = if (name == "PrimaryKey") {
                            args(1) match {
                                case Block(l, _) => createGeneratorLambda(l.head)
                                case _ => createLambda
                            }
                        } else {
                            createLambda
                        }
                        
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

def updateMacroImpl[T <: Product](using q: Quotes, tpe: Type[T]): Expr[(String, List[(String, T => Any)], List[(String, T => Any)])] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    var tableName = sym.name
    val pkFieldExprs = ListBuffer[Expr[String]]()
    val pkFunctionExprs = ListBuffer[Expr[T => Any]]()
    val updateFieldExprs = ListBuffer[Expr[String]]()
    val updateFunctionExprs = ListBuffer[Expr[T => Any]]()

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
                    if (name == "PrimaryKey" || name == "IncrKey" || name == "Column") {
                        val fieldName = args match {
                            case Literal(v) :: _ => v.value.toString()
                            case _ => field.name
                        }

                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[T]), _ => TypeRepr.of[Any])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[T].asTerm
                            Select.unique(x, field.name)
                        }
                        val lambda = Lambda(field, mtpe, rhsFn).asExprOf[T => Any]

                        if (name == "PrimaryKey" || name == "IncrKey") {
                            pkFieldExprs.addOne(Expr.apply(fieldName))
                            pkFunctionExprs.addOne(lambda)
                        } else {
                            updateFieldExprs.addOne(Expr.apply(fieldName))
                            updateFunctionExprs.addOne(lambda)
                        }
                    }
                case _ =>
            }
        }
    }

    if (pkFieldExprs.size == 0) {
        report.error(s"实体类${sym.name}中没有定义主键字段")
    }

    if (updateFieldExprs.size == 0) {
        report.error(s"实体类${sym.name}中没有需要更新的字段")
    }

    val pkFields = Expr.ofList(pkFieldExprs.toList)
    val pkFunctions = Expr.ofList(pkFunctionExprs.toList)
    val updateFields = Expr.ofList(updateFieldExprs.toList)
    val updateFunctions = Expr.ofList(updateFunctionExprs.toList)
    val tableNameExpr = Expr(tableName)

    '{ 
        val pkList = $pkFields.zip($pkFunctions)
        val updateList = $updateFields.zip($updateFunctions)
        ($tableNameExpr, pkList, updateList)
     }
}

@experimental
def pkMacroImpl[T <: Product, PK <: SqlDataType | Tuple](using q: Quotes, t: Type[T], p: Type[PK]): Expr[(String, List[String])] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    var tableName = sym.name
    val pkFieldExprs = ListBuffer[Expr[String]]()

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
    var argTypeNames = ListBuffer[String]()
    if (TypeRepr.of[PK].typeSymbol.name.startsWith("Tuple")) {
        val symTree = TypeRepr.of[PK].termSymbol.tree
        symTree match {
            case ValDef(_, vt, _) => 
                val args = vt.tpe.typeArgs.map(arg => arg.typeSymbol.name)
                argTypeNames.addAll(args)
        }
    } else {
        argTypeNames.addOne(TypeRepr.of[PK].typeSymbol.name)
    }

    val pkTypeNames = ListBuffer[String]()
    
    fields.foreach { field =>
        if (field.annotations.size > 0) {
            val annotation = field.annotations.head
            annotation match {
                case Apply(Select(New(TypeIdent(name)), _), args) =>
                    if (name == "PrimaryKey" || name == "IncrKey") {
                        val fieldName = args match {
                            case Literal(v) :: _ => v.value.toString()
                            case _ => field.name
                        }
                        pkFieldExprs.addOne(Expr.apply(fieldName))
                        field.tree match {
                            case vd: ValDef => pkTypeNames.addOne(vd.tpt.tpe.typeSymbol.name)
                        }
                    }
                case _ =>
            }
        }
    }

    if (pkFieldExprs.size == 0) {
        report.error(s"实体类${sym.name}中没有定义主键字段")
    }

    if (pkTypeNames.size != argTypeNames.size) {
        report.error(s"参数类型与实体类${sym.name}中定义的主键类型不一致")
    } else {
        pkTypeNames.zip(argTypeNames).foreach { (pt, at) =>
            if (pt != at) {
                report.error(s"参数类型与实体类${sym.name}中定义的主键类型不一致")
            }
        }
    }

    val pkFieldsExpr = Expr.ofList(pkFieldExprs.toList)
    val tableNameExpr = Expr(tableName)

    '{
        $tableNameExpr -> $pkFieldsExpr
    }
}