package org.easysql.macros

import org.easysql.ast.SqlDataType
import org.easysql.database.TableEntity
import org.easysql.dsl.{PrimaryKeyColumnExpr, TableColumnExpr, const}
import org.easysql.query.delete.Delete
import org.easysql.util.anyToExpr

import java.sql.SQLException
import scala.quoted.{Expr, Quotes, Type}

def deleteMacroImpl[T <: TableEntity[_]](delete: Expr[Delete], primaryKey: Expr[Any])(using quotes: Quotes, tpe: Type[T]): Expr[Delete] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    val comp = sym.companionClass
    val body = comp.tree.asInstanceOf[ClassDef].body
    val typeName = Expr(sym.name)

    val idents =
        for case deff @ ValDef(name, _, _) <- body
            yield name -> Ref(deff.symbol)

    val tableName = idents.find(ident => ident._1 == "tableName").map(ident => ident._2.asExprOf[String]).get

    val identsExpr: Expr[List[Any]] = Expr.ofList(idents.map(_._2.asExpr))

    '{
        val pkCols = $identsExpr
            .filter(it => it.isInstanceOf[PrimaryKeyColumnExpr[_, _]])
            .map(it => it.asInstanceOf[PrimaryKeyColumnExpr[_, _]])

        if (pkCols.isEmpty) {
            throw SQLException(s"实体类${$typeName}伴生对象中未定义主键字段")
        }
    
        val del = $delete.deleteFrom(${tableName})

        if ($primaryKey.isInstanceOf[Tuple]) {
            val pkValues = $primaryKey.asInstanceOf[Tuple].productIterator.toList
            if (pkValues.size != pkCols.size) {
                throw SQLException(s"实体类${$typeName}中定义的主键字段数目与伴生对象不相符")
            }
            for (i <- pkValues.indices) {
                del.where(pkCols(i).equal(pkValues(i)))
            }
            del
        } else {
            val pkCol = pkCols.head

            del.where(pkCol.equal($primaryKey))
        }
    }
}
