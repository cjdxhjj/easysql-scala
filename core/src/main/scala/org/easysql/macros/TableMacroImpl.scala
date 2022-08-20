package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

import scala.quoted.{Expr, Quotes, Type}

def columnsMacroImpl[T <: TableSchema](table: Expr[T])(using quotes: Quotes, tpe: Type[T]): Expr[Map[String, TableColumnExpr[_]]] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    val comp = sym.companionClass
    val body = comp.tree.asInstanceOf[ClassDef].body

    val idents =
        for case deff @ ValDef(name, _, _) <- body
            yield name

    val compNamesExpr = Expr.ofList(idents.map(Expr(_)))

    '{
        $compNamesExpr.zip($table.$columns).toMap
    }
}

def aliasMacroImpl[T <: TableSchema](name: Expr[String])(using quotes: Quotes, tpe: Type[T]): Expr[T] = {
    import quotes.reflect.*

    val className = TypeRepr.of[T].typeSymbol.fullName
    val classSym = Symbol.requiredClass(className)
    val tree = Apply(Select.unique(New(TypeIdent(classSym)), "<init>"), Expr.apply(name.value).asTerm :: Nil)
    tree.asExprOf[T]
}

