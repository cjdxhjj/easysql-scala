package org.easysql.macros

import org.easysql.ast.expr.SqlIdentifierExpr
import org.easysql.database.TableEntity
import org.easysql.dsl.{PrimaryKeyColumnExpr, TableColumnExpr}
import org.easysql.util.anyToExpr
import org.easysql.visitor.getExpr
import org.easysql.query.insert.Insert

import scala.quoted.{Expr, Quotes, Type}

def insertMacroImpl[T <: TableEntity[_]](insert: Expr[Insert[_, _]], entities: Expr[Seq[T]])(using quotes: Quotes, tpe: Type[T]): Expr[Insert[_, _]] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    val comp = sym.companionClass
    val body = comp.tree.asInstanceOf[ClassDef].body

    val idents =
        for case deff @ ValDef(name, _, _) <- body
            yield Expr(name) -> Ref(deff.symbol)

    val namesExpr: Expr[List[String]] = Expr.ofList(idents.map(_._1.asExprOf[String]))

    val tableName = idents.find(ident => Expr.unapply(ident._1).get == "tableName").map(ident => ident._2.asExprOf[String]).get

    val identsExpr: Expr[List[Any]] = Expr.ofList(idents.map(_._2.asExpr))

    '{
        val columnExprs = $namesExpr.zip($identsExpr).toMap
        val i = $insert
        i.sqlInsert.table = Some(SqlIdentifierExpr($tableName))

        val pkCols = columnExprs
            .filter(it => it._2.isInstanceOf[PrimaryKeyColumnExpr[_, _]])
            .map(it => it._1 -> it._2.asInstanceOf[PrimaryKeyColumnExpr[_, _]])
            .filter(it => !it._2.isIncr)

        pkCols.foreach { it =>
            i.sqlInsert.columns.addOne(SqlIdentifierExpr(it._2.column))
        }

        val columns = columnExprs
            .filter(it => it._2.isInstanceOf[TableColumnExpr[_, _]])
            .map(it => it._1 -> it._2.asInstanceOf[TableColumnExpr[_, _]])

        columns.foreach { it =>
            i.sqlInsert.columns.addOne(SqlIdentifierExpr(it._2.column))
        }
    
        val columnNames = pkCols.map(_._1) ++ columns.map(_._1)

        $entities.foreach { entityItem =>
            val row = valuesMacro(entityItem)
            val value = columnNames.map { it =>
                getExpr(anyToExpr(row(it)))
            }.toList
            i.sqlInsert.values.addOne(value)
        }

        i
    }
}

def valuesMacroImpl[T <: TableEntity[_]](entity: Expr[T])(using quotes: Quotes, tpe: Type[T]): Expr[Map[String, Any]] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    val names = sym.caseFields.map(it => it.name)
    val namesExpr = Expr.ofList(names.map(Expr(_)))

    val tree = entity.asTerm
    val fields = names.map(name => Select.unique(tree, name).asExpr)
    val fieldExprs = Expr.ofList(fields)

    '{
        $namesExpr.zip($fieldExprs).toMap
    }
}
