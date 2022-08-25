package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

import scala.quoted.{Expr, Quotes, Type}

def aliasMacroImpl[T <: TableSchema[_]](using quotes: Quotes, tpe: Type[T]): Expr[T] = {
    import quotes.reflect.*

    val className = TypeRepr.of[T].typeSymbol.fullName
    val classSym = Symbol.requiredClass(className)
    val tree = Apply(Select.unique(New(TypeIdent(classSym)), "<init>"), Nil)
    tree.asExprOf[T]
}

