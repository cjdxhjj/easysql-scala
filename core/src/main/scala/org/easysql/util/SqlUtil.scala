package org.easysql.util

import org.easysql.ast.statement.SqlStatement
import org.easysql.database.DB
import org.easysql.dsl.{Expr, ListExpr, const}
import org.easysql.visitor.outputVisitor.*
import org.easysql.ast.SqlDataType

import java.sql.SQLException
import java.util.Date

def getOutPutVisitor(db: DB): SqlVisitor = db match {
    case DB.MYSQL => MysqlVisitor()
    case DB.PGSQL => PgsqlVisitor()
    case DB.SQLSERVER => SqlserverVisitor()
    case DB.SQLITE => SqliteVisitor()
    case DB.ORACLE => OracleVisitor()
}

def toSqlString(sqlStatement: SqlStatement, db: DB): String = {
    val visitor = getOutPutVisitor(db)
    visitor.visitSqlStatement(sqlStatement)
    visitor.sql()
}

def anyToExpr[T](value: T): Expr[_] = value match {
    case s: SqlDataType => const(s)
    case o: Option[_] => o match {
        case None => const(null)
        case Some(value) => anyToExpr(value)
    }
    case list: List[_] => ListExpr(list.map(it => anyToExpr(it)))
}

def camelToSnake(s: List[Char]): List[Char] = s match {
    case x :: y :: t if y.isUpper => x.toLower :: '_' :: camelToSnake(y :: t)
    case h :: t => h.toLower :: camelToSnake(t)
    case Nil => Nil
}

def camelToSnake(s: String): String = camelToSnake(s.toList).mkString