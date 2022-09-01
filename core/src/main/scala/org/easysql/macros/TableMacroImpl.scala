package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

import scala.quoted.{Expr, Quotes, Type}
import scala.annotation.experimental

def aliasMacroImpl[T <: TableSchema[_]](using quotes: Quotes, tpe: Type[T]): Expr[T] = {
    import quotes.reflect.*

    val className = TypeRepr.of[T].typeSymbol.fullName
    val classSym = Symbol.requiredClass(className)
    val tree = Apply(Select.unique(New(TypeIdent(classSym)), "<init>"), Nil)
    tree.asExprOf[T]
}

def fetchTableNameMacroImpl[T <: Product](using quotes: Quotes, tpe: Type[T]): Expr[String] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    var tableName = sym.name

    if (sym.annotations.size > 0) {
        val annotation = sym.annotations(1)
        annotation match {
            case Apply(Select(New(TypeIdent(name)), _), args) =>
                if (name == "Table") {
                    args match {
                        case Literal(v) :: Nil => tableName = v.value.toString
                        case _ =>
                    }
                }
            case _ =>
        }
    }

    Expr(tableName)
}

@experimental
def exprMetaMacroImpl[T](name: Expr[String])(using q: Quotes, t: Type[T]): Expr[(String, String)] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val eles = sym.declaredFields.map(_.name)
    if (!eles.contains(name.value.get)) {
        report.error(s"value ${name.value.get} is not a member of ${sym.name}")
    }

    val ele = sym.declaredField(name.value.get)

    var eleTag = "column"
    var eleName = name.value.get

    if (ele.annotations.size > 0) {
        val annotation = ele.annotations.head
        annotation match {
            case Apply(Select(New(TypeIdent(name)), _), args) =>
                name match {
                    case "PrimaryKey" => eleTag = "pk"
                    case "IncrKey" => eleTag = "incr"
                    case _ =>
                }

                args match {
                    case Literal(v) :: Nil => eleName = v.value.toString
                    case _ =>
                }
        }
    }

    Expr(eleTag *: eleName *: EmptyTuple)
}

def fieldNamesMacroImpl[T](using q: Quotes, t: Type[T]): Expr[List[String]] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val fields = sym.declaredFields.map { f =>
        var fieldName = f.name
        if (f.annotations.size > 0) {
            val annotation = f.annotations.head
            annotation match {
                case Apply(Select(New(TypeIdent(name)), _), args) =>
                    args match {
                        case Literal(v) :: Nil => fieldName = v.value.toString
                        case _ =>
                    }
            }
        }

        Expr(fieldName)
    }

    Expr.ofList(fields)
}