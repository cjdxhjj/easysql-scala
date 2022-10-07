package org.easysql.visitor.outputVisitor

import org.easysql.ast.statement.select.SqlSelect
import org.easysql.ast.expr.*
import org.easysql.ast.statement.select.SqlSelectItem
import org.easysql.ast.table.SqlIdentifierTableSource
import org.easysql.ast.limit.SqlLimit

import scala.collection.mutable

class ESVisitor {
    val dslBuilder: mutable.StringBuilder = mutable.StringBuilder()

    var spaceNum = 0
    
    def visitSqlSelect(s: SqlSelect) = {
        s.from match {
            case Some(SqlIdentifierTableSource(t)) => dslBuilder.append(s"GET /$t/_search")
            case _ =>
        }
        dslBuilder.append(" {\n")
        spaceNum = 4

        s.limit match {
            case Some(SqlLimit(limit, offset)) => {
                printSpace
                dslBuilder.append(s"\"from\": $offset,")
                dslBuilder.append("\n")
                printSpace
                dslBuilder.append(s"\"size\": $limit,")
                dslBuilder.append("\n")
            }
            case _ =>
        }

        s.selectList.toList match {
            case SqlSelectItem(SqlAllColumnExpr(_), _) :: Nil => {
                printSpace        
                dslBuilder.append("\"_source\": ")
                spaceNum += 4
                dslBuilder.append("[],\n")
            }
            case SqlSelectItem(SqlPropertyExpr(_, _), _) :: xs => {
                printSpace
                dslBuilder.append("\"_source\": ")
                spaceNum += 4
                printSource(s.selectList.toList)
                dslBuilder.append("\n")
            }
            case SqlSelectItem(SqlIdentifierExpr(_), _) :: xs => {
                printSpace
                dslBuilder.append("\"_source\": ")
                spaceNum += 4
                printSource(s.selectList.toList)
                dslBuilder.append("\n")
            }
            case SqlSelectItem(SqlAggFunctionExpr(_, _, _, _, _), _) :: xs => {
                s.groupBy.toList match {
                    case Nil => printAggs(s.selectList.toList)
                    case _ =>
                }
            }
            case _ => spaceNum += 4
        }
        spaceNum = 4

        printSpace
        dslBuilder.append("\"query\": {")
        dslBuilder.append("\n")
        spaceNum += 4
        s.where match {
            case Some(expr) => expr match {
                // todo in和between
                case b: SqlBinaryExpr => printQuery(b)
                case _ => {
                    printSpace
                    dslBuilder.append("\"match_all\": {}")
                }
            }
            case _ => {
                printSpace
                dslBuilder.append("\"match_all\": {}")
            }
        }
        spaceNum -= 4
        printSpace
        dslBuilder.append("\n")
        printSpace
        dslBuilder.append("},")
        dslBuilder.append("\n")
        
        s.groupBy.toList match {
            case x :: xs => printGroup(x, xs, s.selectList.toList)
            case _ =>
        }
        dslBuilder.append("}")
    }

    def printExpr(e: SqlExpr) = {
        e match {
            case SqlIdentifierExpr(name) => dslBuilder.append(s"\"$name\"")
            case SqlPropertyExpr(_, name) => dslBuilder.append(s"\"$name\"")
            case SqlNumberExpr(n) => dslBuilder.append(n.toString())
            case SqlCharExpr(c) => dslBuilder.append(s"\"$c\"")
            case SqlBooleanExpr(b) => dslBuilder.append(b.toString())
            case d: SqlDateExpr => dslBuilder.append(d.toString.replaceAll("'", "\""))
            case _ =>
        }
    }

    def printQuery(e: SqlBinaryExpr): Unit = {
        import org.easysql.ast.expr.SqlBinaryOperator.*

        e.operator match {
            case EQ => {
                printSpace
                dslBuilder.append("\"term\": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                printExpr(e.left)
                dslBuilder.append(": ")
                printExpr(e.right)
                dslBuilder.append("\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
            }
            case LIKE => {
                printSpace
                dslBuilder.append("\"match\": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                printExpr(e.left)
                dslBuilder.append(": ")
                printExpr(e.right)
                dslBuilder.append("\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
            }
            case GT | GE | LT | LE => {
                printSpace
                dslBuilder.append("\"range\": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                printExpr(e.left)
                dslBuilder.append(": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                e.operator match {
                    case GT => dslBuilder.append("\"gt\"")
                    case GE => dslBuilder.append("\"gte\"")
                    case LT => dslBuilder.append("\"lt\"")
                    case LE => dslBuilder.append("\"lte\"")
                    case _ =>
                }
                dslBuilder.append(": ")
                printExpr(e.right)
                dslBuilder.append("\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
                dslBuilder.append("\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
            }
            case AND => printMustOrShould("must", e)
            case OR => printMustOrShould("should", e)
            case NE => {
                printSpace
                dslBuilder.append("\"bool\": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                dslBuilder.append(s"\"must_not\" [\n")
                spaceNum += 4
                printQuery(SqlBinaryExpr(e.left, EQ, e.right))
                spaceNum -= 4
                dslBuilder.append("\n")
                printSpace
                dslBuilder.append("],\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
            }
            case NOT_LIKE => {
                printSpace
                dslBuilder.append("\"bool\": {")
                dslBuilder.append("\n")
                spaceNum += 4
                printSpace
                dslBuilder.append(s"\"must_not\" [\n")
                spaceNum += 4
                printQuery(SqlBinaryExpr(e.left, LIKE, e.right))
                spaceNum -= 4
                dslBuilder.append("\n")
                printSpace
                dslBuilder.append("],\n")
                spaceNum -= 4
                printSpace
                dslBuilder.append("},")
            }
            case _ =>
        }
    }

    def printMustOrShould(key: String, e: SqlBinaryExpr) = {
        printSpace
        dslBuilder.append("\"bool\": {")
        dslBuilder.append("\n")
        spaceNum += 4
        printSpace
        dslBuilder.append(s"\"$key\" [\n")
        spaceNum += 4
        printQuery(e.left.asInstanceOf[SqlBinaryExpr])
        dslBuilder.append("\n")
        printQuery(e.right.asInstanceOf[SqlBinaryExpr])
        spaceNum -= 4
        dslBuilder.append("\n")
        printSpace
        dslBuilder.append("],\n")
        spaceNum -= 4
        printSpace
        dslBuilder.append("},")
    }

    def printSource(l: List[SqlSelectItem]) = {
        val sourceList = l filter { i => 
            i match {
                case SqlSelectItem(SqlIdentifierExpr(_), _) => true
                case SqlSelectItem(SqlPropertyExpr(_, _), _) => true
                case _ => false
            }
        } map { i => 
            i match {
                case SqlSelectItem(SqlIdentifierExpr(name), _) => name
                case SqlSelectItem(SqlPropertyExpr(_, name), _) => name
                case _ => ""
            }
        }
        dslBuilder.append(s"[${sourceList.mkString(", ")}],")
    }

    def printGroup(head: SqlExpr, tail: List[SqlExpr], l: List[SqlSelectItem]): Unit = {
        printSpace
        dslBuilder.append("\"aggregations\": {\n")
        spaceNum += 4
        printSpace
        dslBuilder.append(s"\"${getGroupName(head)}\": {\n")
        spaceNum += 4
        printSpace
        dslBuilder.append("\"terms\": {\n")
        spaceNum += 4
        printSpace
        dslBuilder.append(s"\"field\": ")
        printExpr(head)
        dslBuilder.append("\n")
        spaceNum -= 4
        printSpace
        dslBuilder.append("},\n")
        tail match {
            case x :: xs => printGroup(x, xs, l)
            case Nil => printAggs(l)
        }
        spaceNum -= 4
        printSpace
        dslBuilder.append("},\n")
        spaceNum -= 4
        printSpace
        dslBuilder.append("},\n")
    }

    def printAggs(l: List[SqlSelectItem]) = {
        printSpace
        dslBuilder.append("\"aggregations\": {\n")
        l.foreach { i =>
            spaceNum += 4
            printSpace
            dslBuilder.append(s"\"${i.alias.get}\": {\n")
            spaceNum += 4
            i.expr match {
                case SqlAggFunctionExpr(n, x :: Nil, d, _, _) => {
                    printSpace
                    (n, d) match {
                        case ("COUNT", false) => dslBuilder.append("\"value_count\": {\n")
                        case ("COUNT", true) => dslBuilder.append("\"cardinality\": {\n")
                        case ("SUM", _) => dslBuilder.append("\"sum\": {\n")
                        case ("MAX", _) => dslBuilder.append("\"max\": {\n")
                        case ("MIN", _) => dslBuilder.append("\"min\": {\n")
                        case ("AVG", _) => dslBuilder.append("\"avg\": {\n")
                        case _ => dslBuilder.append("\"\": {\n")
                    }
                    spaceNum += 4
                    printSpace
                    dslBuilder.append("\"field\": ")
                    printExpr(x)
                    dslBuilder.append("\n")
                    spaceNum -= 4
                    printSpace
                    dslBuilder.append("},\n")
                }
                case _ =>
            }
            spaceNum -= 4
            printSpace
            dslBuilder.append("},\n")
            spaceNum -= 4
        }
        printSpace
        dslBuilder.append("},\n")
    }

    def getGroupName(e: SqlExpr): String = e match {
        case SqlIdentifierExpr(name) => name + "_group"
        case SqlPropertyExpr(_, name) => name + "_group"
        case _ => ""
    }

    def printSpace: Unit = {
        if (spaceNum > 0) {
            for (_ <- 1 to spaceNum) {
                dslBuilder.append(" ")
            }
        }
    }
}