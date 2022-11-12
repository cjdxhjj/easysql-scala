package org.easysql.util

import org.easysql.ast.statement.SqlStatement
import org.easysql.database.DB
import org.easysql.dsl.{Expr, ListExpr, const}
import org.easysql.visitor.outputVisitor.*

import java.sql.SQLException
import java.util.Date

def getOutPutVisitor(db: DB): SqlVisitor = {
    db match {
        case DB.MYSQL => MysqlVisitor()
        case DB.PGSQL => PgsqlVisitor()
        case DB.SQLSERVER => SqlserverVisitor()
        case DB.SQLITE => SqliteVisitor()
        case DB.ORACLE => OracleVisitor()
    }
}

def toSqlString(sqlStatement: SqlStatement, db: DB): String = {
    val visitor = getOutPutVisitor(db)
    visitor.visitSqlStatement(sqlStatement)
    visitor.sql()
}

def anyToExpr(value: Any): Expr[_] = {
    value match {
        case null => const(null)
        case s: String => const(s)
        case i: Int => const(i)
        case l: Long => const(l)
        case d: Double => const(d)
        case f: Float => const(f)
        case b: Boolean => const(b)
        case d: Date => const(d)
        case dc: BigDecimal => const(dc)
        case o: Option[_] =>
            if (o.isEmpty) {
                const(null)
            } else {
                o.get match {
                    case s: String => const(s)
                    case i: Int => const(i)
                    case l: Long => const(l)
                    case d: Double => const(d)
                    case f: Float => const(f)
                    case b: Boolean => const(b)
                    case d: Date => const(d)
                    case _ => throw SQLException("cannot convert to type of sql expression")
                }
            }
        case list: List[_] => ListExpr(list.map(it => anyToExpr(it)))
        case _ => throw SQLException("cannot convert to type of sql expression")
    }
}

def camelToSnake(s: List[Char]): List[Char] = s match {
    case x :: y :: t if y.isUpper => x.toLower :: '_' :: camelToSnake(y :: t)
    case h :: t => h.toLower :: camelToSnake(t)
    case Nil => Nil
}

def camelToSnake(s: String): String = camelToSnake(s.toList).mkString