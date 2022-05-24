package org.easysql.ast

import org.easysql.ast.expr.{SqlBooleanExpr, SqlCharExpr, SqlExpr, SqlListExpr, SqlNumberExpr}

import java.util.Date

type SqlConstType = String | Int | Long | Double | Float | Boolean | List[String | Int | Long | Double | Float | Boolean | Date] | Date | Null

type SqlSingleConstType = String | Int | Long | Double | Float | Boolean | Date | BigDecimal