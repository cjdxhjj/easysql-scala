package org.easysql.macros

import org.easysql.database.TableEntity
import org.easysql.dsl.{PrimaryKeyColumnExpr, TableColumnExpr}

import scala.quoted.{Expr, Quotes, Type}
import java.util.Date

def bindEntityMacroImpl[A <: TableEntity[_]](using q: Quotes, tpe: Type[A]): Expr[Map[String, Any | Null] => A] = {
    import q.reflect.*

    val aTpr = TypeRepr.of[A]
    val ctor = aTpr.typeSymbol.primaryConstructor
    val typeSymbol = aTpr.typeSymbol
    val companion = typeSymbol.companionModule
    val compDecl = companion.declaredFields
    val sym = TypeTree.of[A].symbol
    val comp = sym.companionClass
    val names =
        for p <- sym.caseFields if p.flags.is(Flags.HasDefault)
            yield p.name
    val body = comp.tree.asInstanceOf[ClassDef].body
    val idents: List[Ref] =
        for
            case deff @ DefDef(name, _, _, _) <- body
            if name.startsWith("$lessinit$greater$default")
        yield Ref(deff.symbol)
    val identsExpr = idents.map(_.asExpr)
    val defaultMap = names.zip(identsExpr).toMap

    val columns = compDecl.filter(symbol =>
        symbol.tree match {
            case vd: ValDef => {
                vd.tpt.tpe.asType match {
                    case '[TableColumnExpr[_, _]] => !symbol.flags.is(Flags.Protected | Flags.Local)
                    case _ => false
                }
            }
        }
    )

    val pkColumns = compDecl.filter(symbol =>
        symbol.tree match {
            case vd: ValDef => {
                vd.tpt.tpe.asType match {
                    case '[PrimaryKeyColumnExpr[_, _]] => !symbol.flags.is(Flags.Protected | Flags.Local)
                    case _ => false
                }
            }
        }
    )

    val columnsMap = columns.map { column =>
        column.name -> Select.unique(This(companion.fieldMember(column.name).owner), column.name).asExprOf[TableColumnExpr[_, _]]
    }.toMap

    val pkColumnsMap = pkColumns.map { column =>
        column.name -> Select.unique(This(companion.fieldMember(column.name).owner), column.name).asExprOf[PrimaryKeyColumnExpr[_, _]]
    }.toMap

    val typ = TypeRepr.of[String]
    '{
        (map: Map[String, Any | Null]) => ${
            val terms: List[Expr[_]] = ctor.paramSymss.head.map { symbol =>
                symbol.tree match {
                    case vd: ValDef => {
                        val tpe = vd.tpt.tpe
                        tpe.asType match {
                            case '[String] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[String]]
                                    } else {
                                        '{ "" }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[String]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[String]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Int] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Int]]
                                    } else {
                                        '{ 0 }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Int]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Int]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Long] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Long]]
                                    } else {
                                        '{ 0L }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Long]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Long]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Double] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Double]]
                                    } else {
                                        '{ 0.0D }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Double]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Double]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Float] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Float]]
                                    } else {
                                        '{ 0.0F }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Float]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Float]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[BigDecimal] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[BigDecimal]]
                                    } else {
                                        '{ 0 }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[BigDecimal]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[BigDecimal]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Boolean] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Boolean]]
                                    } else {
                                        '{ false }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Boolean]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Boolean]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Date] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Date]]
                                    } else {
                                        '{ new Date() }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Date]
                                        } else { $nonContain }
                                    }
                                } else if (pkColumnsMap.contains(vd.name)) {
                                    val column = pkColumnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            map(columnName).asInstanceOf[Date]
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[String]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[String]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[String])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Int]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Int]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Int])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Long]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Long]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Long])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Double]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Double]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Double])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Float]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Float]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Float])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[BigDecimal]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[BigDecimal]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[BigDecimal])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Boolean]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Boolean]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Boolean])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case '[Option[Date]] =>
                                val nonContain = {
                                    if (symbol.flags.is(Flags.HasDefault)) {
                                        defaultMap(vd.name).asInstanceOf[Expr[Option[Date]]]
                                    } else {
                                        '{ None }
                                    }
                                }
                                if (columnsMap.contains(vd.name)) {
                                    val column = columnsMap(vd.name)
                                    '{
                                        val columnName = $column.column
                                        if (map.contains(columnName)) {
                                            val cell = map(columnName)
                                            if (cell == null) {
                                                None
                                            } else {
                                                Some(cell.asInstanceOf[Date])
                                            }
                                        } else { $nonContain }
                                    }
                                } else {
                                    nonContain
                                }

                            case _ => report.errorAndAbort(s"实体类${aTpr.typeSymbol.name}中存在非法数据类型：${symbol.tree.show}")
                        }
                    }
                }
            }
            New(Inferred(aTpr)).select(ctor).appliedToArgs(terms.map(_.asTerm)).asExprOf[A]
        }
    }
}
