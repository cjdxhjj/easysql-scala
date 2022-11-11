package org.easysql.visitor.outputVisitor

import org.easysql.ast.statement.select.SqlSelect
import org.easysql.ast.table.*
import org.easysql.ast.expr.*
import org.easysql.ast.order.*

import scala.collection.mutable

class MongoVisitor {
    val dslBuilder: mutable.StringBuilder = mutable.StringBuilder()

    var spaceNum = 0

    def visitSqlSelect(s: SqlSelect) = {
        s.from match {
            case Some(SqlIdentTable(t)) => dslBuilder.append(s"db.$t.find(")
            case _ =>
        }
        s.where match {
            case Some(expr: SqlBinaryExpr) => {
                visitSqlBinaryExpr(expr)
                dslBuilder.append(", ")
            }
            case Some(expr: SqlInExpr) => {
                visitSqlInExpr(expr)
                dslBuilder.append(", ")
            }
            case _ => dslBuilder.append("{}, ")
        }
        if (s.selectList.isEmpty) {
            dslBuilder.append("{}")
        } else {
            dslBuilder.append("{")
            val selectItems = s.selectList.filter { item =>
                item.expr match {
                    case _: SqlIdentExpr => true
                    case _: SqlPropertyExpr => true
                    case _ => false 
                }
            }.map { item =>
                item.expr match {
                    case SqlIdentExpr(name) => s"\"$name\": 1"
                    case SqlPropertyExpr(_, name) => s"\"$name\": 1"
                    case _ => ""
                }
            }.reduce((i, acc) => i + ", " + acc)
            dslBuilder.append(s"$selectItems}")
        }
        dslBuilder.append(")")
        if (s.orderBy.nonEmpty) {
            dslBuilder.append(".sort({")
            for (i <- 0 until s.orderBy.size) {
                printExpr(s.orderBy(i).expr)
                dslBuilder.append(": ")
                if (s.orderBy(i).order == SqlOrderByOption.ASC) {
                    dslBuilder.append("1")
                } else {
                    dslBuilder.append("-1")
                }
                if (i < s.orderBy.size - 1) {
                    dslBuilder.append(", ")
                }
            }
            dslBuilder.append("})")
        }
        s.limit.foreach { l =>
            dslBuilder.append(s".limit(${l.limit}).skip(${l.offset})")
        }
    }

    def printExpr(e: SqlExpr): Unit = {
        e match {
            case SqlIdentExpr(name) => dslBuilder.append(s"\"$name\"")
            case SqlPropertyExpr(_, name) => dslBuilder.append(s"\"$name\"")
            case SqlNumberExpr(n) => dslBuilder.append(n.toString())
            case SqlCharExpr(c) => dslBuilder.append(s"\"$c\"")
            case SqlBooleanExpr(b) => dslBuilder.append(b.toString())
            case d: SqlDateExpr => dslBuilder.append(d.toString.replaceAll("'", "\""))
            case b: SqlBinaryExpr => visitSqlBinaryExpr(b)
            case l: SqlListExpr[_] => {
                dslBuilder.append("[")
                for (i <- 0 until l.items.size) {
                    printExpr(l.items(i))
                    if (i < l.items.size - 1) {
                        dslBuilder.append(", ")
                    }
                }
                dslBuilder.append("]")
            }
            case i: SqlInExpr => visitSqlInExpr(i)
            case _ =>
        }
    }

    def visitSqlInExpr(e: SqlInExpr): Unit = {
        val operator = e.isNot match {
            case true => "$nin"
            case false => "$in"
        }
        dslBuilder.append("{")
        printExpr(e.expr)
        dslBuilder.append(s": {$operator: ")
        printExpr(e.inExpr)
        dslBuilder.append("}}")
    }

    def visitSqlBinaryExpr(e: SqlBinaryExpr): Unit = {
        import org.easysql.ast.expr.SqlBinaryOperator.*

        e.operator match {
            case AND => {
                dslBuilder.append("{$and: [")
                printExpr(e.left)
                dslBuilder.append(", ")
                printExpr(e.right)
                dslBuilder.append("]}")
            }
            case OR => {
                dslBuilder.append("{$or: [")
                printExpr(e.left)
                dslBuilder.append(", ")
                printExpr(e.right)
                dslBuilder.append("]}")
            }
            case EQ => {
                dslBuilder.append("{")
                printExpr(e.left)
                dslBuilder.append(": ")
                printExpr(e.right)
                dslBuilder.append("}")
            }
            case NE | GT | GE | LT | LE => {
                val operator = e.operator match {
                    case NE => "$ne"
                    case GT => "$gt"
                    case GE => "$gte"
                    case LT => "$lt"
                    case LE => "$lte"
                    case _ => ""
                }
                dslBuilder.append("{")
                printExpr(e.left)
                dslBuilder.append(s": {$operator: ")
                printExpr(e.right)
                dslBuilder.append("}}")
            }
            case _ =>
        }
    }

    def printSpace: Unit = {
        if (spaceNum > 0) {
            for (_ <- 1 to spaceNum) {
                dslBuilder.append(" ")
            }
        }
    }
}
