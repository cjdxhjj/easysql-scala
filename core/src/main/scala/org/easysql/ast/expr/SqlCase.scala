package org.easysql.ast.expr

case class SqlCase(expr: SqlExpr, thenExpr: SqlExpr)