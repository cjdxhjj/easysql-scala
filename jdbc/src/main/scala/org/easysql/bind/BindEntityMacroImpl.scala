package org.easysql.bind

import org.easysql.dsl.{TableSchema, PrimaryKeyColumnExpr, TableColumnExpr}

import java.util.Date
import java.sql.ResultSet
import scala.quoted.*

def bindEntityMacroImpl[T](nextIndex: Expr[Int])(using q: Quotes, tpe: Type[T]): Expr[(Int, Array[Any] => T)] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val tpr = TypeRepr.of[T]
    val ctor = tpr.typeSymbol.primaryConstructor
    val fields = sym.declaredFields
    var i = 0
    val bindFunctionExprs = fields map { f =>
        f.tree match {
            case vd: ValDef => {
                val vdt = vd.tpt.tpe.asType
                vdt match {
                    case '[Option[t]] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Array[Any]]), _ => TypeRepr.of[Option[t]])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Array[Any]]
                            val offset = Expr(i)
                            '{ if $x($nextIndex + $offset) == null then None else Some($x($nextIndex + $offset)).asInstanceOf[Option[t]] }.asTerm
                        }
                        val lambda = Lambda(f, mtpe, rhsFn).asExprOf[Array[Any] => Option[t]]
                        i = i + 1
                        lambda
                    }

                    case '[t] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Array[Any]]), _ => TypeRepr.of[t])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Array[Any]]
                            val offset = Expr(i)
                            '{ $x($nextIndex + $offset).asInstanceOf[t] }.asTerm
                        }
                        val lambda = Lambda(f, mtpe, rhsFn).asExprOf[Array[Any] => t]
                        i = i + 1
                        lambda
                    }
                }
            }
        }
    }

    val next = Expr(i)

    '{
        val bindFunc = (result: Array[Any]) =>
            ${
                val terms = bindFunctionExprs.map { f => 
                    '{ $f.apply(result) }.asTerm 
                }
                New(Inferred(tpr)).select(ctor).appliedToArgs(terms).asExprOf[T]
            }

        $next + $nextIndex -> bindFunc
    }
}

def bindOptionMacroImpl[T](nextIndex: Expr[Int])(using q: Quotes, tpe: Type[T]): Expr[(Int, Array[Any] => Option[T])] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val tpr = TypeRepr.of[T]
    val ctor = tpr.typeSymbol.primaryConstructor
    val fields = sym.declaredFields
    val fieldSize = Expr(fields.size)
    var i = 0
    val bindFunctionExprs = fields map { f =>
        f.tree match {
            case vd: ValDef => {
                val vdt = vd.tpt.tpe.asType
                vdt match {
                    case '[Option[t]] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Array[Any]]), _ => TypeRepr.of[Option[t]])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Array[Any]]
                            val offset = Expr(i)
                            '{ if $x($nextIndex + $offset) == null then None else Some($x($nextIndex + $offset)).asInstanceOf[Option[t]] }.asTerm
                        }
                        val lambda = Lambda(f, mtpe, rhsFn).asExprOf[Array[Any] => Option[t]]
                        i = i + 1
                        lambda
                    }

                    case '[t] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Array[Any]]), _ => TypeRepr.of[t])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Array[Any]]
                            val offset = Expr(i)
                            '{ $x($nextIndex + $offset).asInstanceOf[t] }.asTerm
                        }
                        val lambda = Lambda(f, mtpe, rhsFn).asExprOf[Array[Any] => t]
                        i = i + 1
                        lambda
                    }
                }
            }
        }
    }

    val next = Expr(i)

    '{
        val bindFunc = (result: Array[Any]) =>
            ${
                val terms = bindFunctionExprs.map { f => 
                    '{ $f.apply(result) }.asTerm 
                }
                New(Inferred(tpr)).select(ctor).appliedToArgs(terms).asExprOf[T]
            }

        val bind = (result: Array[Any]) => {
            val isNull = 
                result.slice($nextIndex, $nextIndex + $fieldSize).map(i => i == null).reduce((l, r) => l && r)
            if isNull then None else Some(bindFunc(result))
        }

        $next + $nextIndex -> bind
    }
}