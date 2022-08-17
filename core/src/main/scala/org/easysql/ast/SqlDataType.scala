package org.easysql.ast

import org.easysql.ast.expr.{SqlBooleanExpr, SqlCharExpr, SqlExpr, SqlListExpr, SqlNumberExpr}

import java.util.Date

type SqlDataType = String | Int | Long | Double | Float | Boolean | Date | BigDecimal

type SqlNumberType = Int | Long | Float | Double | BigDecimal