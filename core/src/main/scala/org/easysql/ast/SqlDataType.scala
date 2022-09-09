package org.easysql.ast

import org.easysql.ast.expr.{SqlBooleanExpr, SqlCharExpr, SqlExpr, SqlListExpr, SqlNumberExpr}

import java.util.Date
import java.math.BigInteger

type SqlNumberType = Int | Long | Double | Float | BigDecimal | Number

type SqlDataType = SqlNumberType | String | Boolean | Date