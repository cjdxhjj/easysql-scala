package org.easysql.macros

import org.easysql.ast.statement.select.*
import org.easysql.ast.expr.*
import org.easysql.visitor.outputVisitor.*

import scala.quoted.*

def astMacroImpl(ast: Expr[SqlSelect])(using q: Quotes): Expr[String] = {
    import q.reflect.*
    
//    report.error(s"${ast.value}")
    val value = ast.value.get
    val visitor = new PgsqlVisitor
    visitor.visitSqlSelect(value)
    val sql = visitor.sql()
    report.warning(sql)
    Expr(sql)
}

