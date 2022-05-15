package org.easysql.macros

import org.easysql.ast.expr.SqlIdentifierExpr
import org.easysql.ast.statement.upsert.SqlUpsert
import org.easysql.database.TableEntity
import org.easysql.query.save.Save
import org.easysql.dsl.{ConstExpr, PrimaryKeyColumnExpr, TableColumnExpr}
import org.easysql.visitor.getExpr
import org.easysql.util.anyToExpr
import org.easysql.util.toSqlString

import java.sql.SQLException
import scala.quoted.{Expr, Quotes, Type}

def saveMacroImpl[T <: TableEntity[_]](save: Expr[Save], entity: Expr[T])(using quotes: Quotes, tpe: Type[T]): Expr[Save] = {
    import quotes.reflect.*

    val sym = TypeTree.of[T].symbol
    val comp = sym.companionClass
    val body = comp.tree.asInstanceOf[ClassDef].body
    val typeName = Expr(sym.name)

    val names = sym.caseFields.map(it => it.name)
    val namesExpr = Expr.ofList(names.map(Expr(_)))

    val tree = entity.asTerm
    val fields = names.map(name => Select.unique(tree, name).asExpr)
    val fieldExprs = Expr.ofList(fields)

    val idents =
        for case deff @ ValDef(name, _, _) <- body
            yield Expr(name) -> Ref(deff.symbol)

    val compNamesExpr: Expr[List[String]] = Expr.ofList(idents.map(_._1.asExprOf[String]))

    val tableName = idents.find(ident => Expr.unapply(ident._1).get == "tableName").map(ident => ident._2.asExprOf[String]).get

    val identsExpr: Expr[List[Any]] = Expr.ofList(idents.map(_._2.asExpr))

    '{
        val columnExprs = $compNamesExpr.zip($identsExpr).toMap
        val properties = columnExprs
            .filter(it => it._2.isInstanceOf[TableColumnExpr[_]])
            .map(it => it._1 -> it._2.asInstanceOf[TableColumnExpr[_]])
            .toMap

        val pkCols = columnExprs
            .filter(it => it._2.isInstanceOf[PrimaryKeyColumnExpr[_]])
            .map(it => it._1 -> it._2.asInstanceOf[PrimaryKeyColumnExpr[_]])
            .toMap

        if (pkCols.isEmpty) {
            // todo 将报错提升到编译期
            throw SQLException(s"实体类${$typeName}伴生对象中未定义主键字段")
        }

        val columns = pkCols.map(it => SqlIdentifierExpr(it._2.column)) ++ properties.map(it => SqlIdentifierExpr(it._2.column))
        val updateColumns = properties.map(it => SqlIdentifierExpr(it._2.column))
        val entityProperties = $namesExpr.zip($fieldExprs).toMap
        val value = pkCols.map(it => getExpr(anyToExpr(entityProperties(it._1)))) ++ properties.map(it => getExpr(anyToExpr(entityProperties(it._1))))

        val upsert = $save.sqlUpsert
        upsert.table = Some(SqlIdentifierExpr($tableName))
        upsert.columns.addAll(columns)
        upsert.primaryColumns.addAll(pkCols.map(it => SqlIdentifierExpr(it._2.column)))
        upsert.updateColumns.addAll(updateColumns)
        upsert.value.addAll(value)

        $save
    }
}
