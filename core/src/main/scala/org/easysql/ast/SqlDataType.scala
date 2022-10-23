package org.easysql.ast

import org.easysql.ast.expr.{SqlBooleanExpr, SqlCharExpr, SqlExpr, SqlListExpr, SqlNumberExpr}

import java.util.Date
import java.math.BigInteger

type SqlNumberType = Number | Int | Long | Float | Double | BigDecimal

type SqlDataType = SqlNumberType | String | Boolean | Date