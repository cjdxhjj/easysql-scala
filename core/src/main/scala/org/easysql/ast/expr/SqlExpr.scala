package org.easysql.ast.expr

import org.easysql.ast.SqlNode
import org.easysql.ast.order.SqlOrderBy
import org.easysql.ast.statement.select.SqlSelectQuery

import scala.quoted.*
import java.text.SimpleDateFormat
import java.util.Date

sealed class SqlExpr extends SqlNode

object SqlExpr {
    given SqlFromExpr: FromExpr[SqlExpr] with {
        override def unapply(x: Expr[SqlExpr])(using Quotes): Option[SqlExpr] = x match {
            case '{ $x: SqlIdentifierExpr } => x.value
            case '{ $x: SqlPropertyExpr } => x.value
            case '{ $x: SqlBinaryExpr } => x.value
            case '{ $x: SqlNullExpr } => x.value
            case '{ $x: SqlAllColumnExpr } => x.value
            case '{ $x: SqlNumberExpr } => x.value
            case '{ $x: SqlDateExpr } => x.value
            case '{ $x: SqlCharExpr } => x.value
            case '{ $x: SqlBooleanExpr } => x.value
            case '{ $x: SqlListExpr } => x.value
            case '{ $x: SqlAggFunctionExpr } => x.value
            case '{ $x: SqlExprFunctionExpr } => x.value
            case '{ $x: SqlCastExpr } => x.value
            case '{ $x: SqlSelectQueryExpr } => x.value
            case '{ $x: SqlInExpr } => x.value
            case '{ $x: SqlBetweenExpr } => x.value
            case '{ $x: SqlOverExpr } => x.value
            case '{ $x: SqlCaseExpr } => x.value
            case '{ $x: SqlSubQueryPredicateExpr } => x.value
            case _ => None
        }
    }
}

case class SqlBinaryExpr(left: SqlExpr, operator: SqlBinaryOperator, right: SqlExpr) extends SqlExpr

object SqlBinaryExpr {
    given SqlBinaryFromExpr: FromExpr[SqlBinaryExpr] with {
        override def unapply(x: Expr[SqlBinaryExpr])(using Quotes): Option[SqlBinaryExpr] = x match {
            case '{ SqlBinaryExpr(${Expr(l)}, ${Expr(o)}, ${Expr(r)}) } => Some(SqlBinaryExpr(l, o, r))
            case _ => None
        }
    }
}

case class SqlIdentifierExpr(name: String) extends SqlExpr

object SqlIdentifierExpr {
    given FromExpr[SqlIdentifierExpr] with {
        override def unapply(x: Expr[SqlIdentifierExpr])(using Quotes): Option[SqlIdentifierExpr] = x match {
            case '{ SqlIdentifierExpr(${Expr(n)}) } => Some(SqlIdentifierExpr(n))
            case _ => None
        }
    }
}

case class SqlPropertyExpr(owner: String, name: String) extends SqlExpr

object SqlPropertyExpr {
    given FromExpr[SqlPropertyExpr] with {
        override def unapply(x: Expr[SqlPropertyExpr])(using Quotes): Option[SqlPropertyExpr] = x match {
            case '{ SqlPropertyExpr(${Expr(o)}, ${Expr(n)}) } => Some(SqlPropertyExpr(o, n))
            case _ => None
        }
    }
}

case class SqlNullExpr() extends SqlExpr {
    override def toString = "NULL"
}

object SqlNullExpr {
    given FromExpr[SqlNullExpr] with {
        override def unapply(x: Expr[SqlNullExpr])(using Quotes): Option[SqlNullExpr] = x match {
            case '{ SqlNullExpr() } => Some(SqlNullExpr())
            case _ => None
        }
    }
}

case class SqlAllColumnExpr(var owner: Option[String]) extends SqlExpr

object SqlAllColumnExpr {
    given FromExpr[SqlAllColumnExpr] with {
        override def unapply(x: Expr[SqlAllColumnExpr])(using Quotes): Option[SqlAllColumnExpr] = x match {
            case '{ SqlAllColumnExpr(${Expr(o)}) } => Some(SqlAllColumnExpr(o))
            case _ => None
        }
    }
}

case class SqlNumberExpr(number: Number) extends SqlExpr {
    override def toString: String = number.toString
}

object SqlNumberExpr {
    given FromExpr[BigDecimal] with {
        override def unapply(x: Expr[BigDecimal])(using Quotes): Option[BigDecimal] = x match {
            case '{ $y: BigDecimal } => y.value
            case _ => None
        }
    }

    given FromExpr[SqlNumberExpr] with {
        override def unapply(x: Expr[SqlNumberExpr])(using Quotes): Option[SqlNumberExpr] = x match {
            case '{ SqlNumberExpr(${Expr(n)}: Int) } => Some(SqlNumberExpr(n))
            case '{ SqlNumberExpr(${Expr(n)}: Long) } => Some(SqlNumberExpr(n))
            case '{ SqlNumberExpr(${Expr(n)}: Float) } => Some(SqlNumberExpr(n))
            case '{ SqlNumberExpr(${Expr(n)}: Double) } => Some(SqlNumberExpr(n))
            case '{ SqlNumberExpr(${Expr(n)}: BigDecimal) } => Some(SqlNumberExpr(n))
            case _ => None
        }
    }
}

case class SqlDateExpr(date: Date) extends SqlExpr {
    override def toString: String = {
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        s"'${format.format(date)}'"
    }
}

object SqlDateExpr {
    given FromExpr[Date] with {
        override def unapply(x: Expr[Date])(using Quotes): Option[Date] = x match {
            case '{ $y: Date } => y.value
            case _ => None
        }
    }

    given FromExpr[SqlDateExpr] with {
        override def unapply(x: Expr[SqlDateExpr])(using Quotes): Option[SqlDateExpr] = x match {
            case '{ SqlDateExpr(${Expr(d)}) } => Some(SqlDateExpr(d))
            case _ => None
        }
    }
}

case class SqlCharExpr(text: String) extends SqlExpr {
    override def toString = s"'${text.replace("'", "''")}'"
}

object SqlCharExpr {
    given FromExpr[SqlCharExpr] with {
        override def unapply(x: Expr[SqlCharExpr])(using Quotes): Option[SqlCharExpr] = x match {
            case '{ SqlCharExpr(${Expr(t)}) } => Some(SqlCharExpr(t))
            case _ => None
        }
    }
}

case class SqlBooleanExpr(boolean: Boolean) extends SqlExpr {
    override def toString: String = boolean.toString
}

object SqlBooleanExpr {
    given FromExpr[SqlBooleanExpr] with {
        override def unapply(x: Expr[SqlBooleanExpr])(using Quotes): Option[SqlBooleanExpr] = x match {
            case '{ SqlBooleanExpr(${Expr(b)}) } => Some(SqlBooleanExpr(b))
            case _ => None
        }
    }
}

case class SqlListExpr(items: List[SqlExpr]) extends SqlExpr

object SqlListExpr {
    given SqlListFromExpr: FromExpr[SqlListExpr] with {
        override def unapply(x: Expr[SqlListExpr])(using Quotes): Option[SqlListExpr] = x match {
            case '{ SqlListExpr(${Expr(i)}) } => Some(SqlListExpr(i))
            case _ => None
        }
    }
}

case class SqlAggFunctionExpr(name: String, args: List[SqlExpr], distinct: Boolean, attributes: Map[String, SqlExpr],
                              orderBy: List[SqlOrderBy]) extends SqlExpr

object SqlAggFunctionExpr {
    given FromExpr[SqlAggFunctionExpr] with {
        override def unapply(x: Expr[SqlAggFunctionExpr])(using Quotes): Option[SqlAggFunctionExpr] = x match {
            case '{ SqlAggFunctionExpr(${Expr(n)}, ${Expr(a)}, ${Expr(d)}, ${Expr(at)}, ${Expr(o)}) } => Some(SqlAggFunctionExpr(n, a, d, at, o))
            case _ => None
        }
    }
}

case class SqlExprFunctionExpr(name: String, var args: List[SqlExpr]) extends SqlExpr

object SqlExprFunctionExpr {
    given FromExpr[SqlExprFunctionExpr] with {
        override def unapply(x: Expr[SqlExprFunctionExpr])(using Quotes): Option[SqlExprFunctionExpr] = x match {
            case '{ SqlExprFunctionExpr(${Expr(n)}, ${Expr(a)}) } => Some(SqlExprFunctionExpr(n, a))
            case _ => None
        }
    }
}

case class SqlCastExpr(expr: SqlExpr, castType: String) extends SqlExpr

object SqlCastExpr {
    given FromExpr[SqlCastExpr] with {
        override def unapply(x: Expr[SqlCastExpr])(using Quotes): Option[SqlCastExpr] = x match {
            case '{ SqlCastExpr(${Expr(e)}, ${Expr(c)}) } => Some(SqlCastExpr(e, c))
            case _ => None
        }
    }
}

case class SqlSelectQueryExpr(query: SqlSelectQuery) extends SqlExpr

object SqlSelectQueryExpr {
    given FromExpr[SqlSelectQueryExpr] with {
        override def unapply(x: Expr[SqlSelectQueryExpr])(using Quotes): Option[SqlSelectQueryExpr] = x match {
            case '{ SqlSelectQueryExpr(${Expr(q)}) } => Some(SqlSelectQueryExpr(q))
            case _ => None
        }
    }
}

case class SqlInExpr(expr: SqlExpr, inExpr: SqlExpr, isNot: Boolean) extends SqlExpr

object SqlInExpr {
    given FromExpr[SqlInExpr] with {
        override def unapply(x: Expr[SqlInExpr])(using Quotes): Option[SqlInExpr] = x match {
            case '{ SqlInExpr(${Expr(e)}, ${Expr(i)}, ${Expr(in)}) } => Some(SqlInExpr(e, i, in))
            case _ => None
        }
    }
}

case class SqlBetweenExpr(expr: SqlExpr, start: SqlExpr, end: SqlExpr, isNot: Boolean) extends SqlExpr

object SqlBetweenExpr {
    given SqlBetweenFromExpr: FromExpr[SqlBetweenExpr] with {
        override def unapply(x: Expr[SqlBetweenExpr])(using Quotes): Option[SqlBetweenExpr] = x match {
            case '{ SqlBetweenExpr(${Expr(ex)}, ${Expr(s)}, ${Expr(e)}, ${Expr(i)}) } => Some(SqlBetweenExpr(ex, s, e, i))
            case _ => None
        }
    }
}

case class SqlOverExpr(agg: SqlAggFunctionExpr, partitionBy: List[SqlExpr], orderBy: List[SqlOrderBy]) extends SqlExpr

object SqlOverExpr {
    given FromExpr[SqlOverExpr] with {
        override def unapply(x: Expr[SqlOverExpr])(using Quotes): Option[SqlOverExpr] = x match {
            case '{ SqlOverExpr(${Expr(a)}, ${Expr(p)}, ${Expr(o)}) } => Some(SqlOverExpr(a, p, o))
            case _ => None
        }
    }
}

case class SqlCaseExpr(caseList: List[SqlCase], default: SqlExpr) extends SqlExpr

object SqlCaseExpr {
    given FromExpr[SqlCaseExpr] with {
        override def unapply(x: Expr[SqlCaseExpr])(using Quotes): Option[SqlCaseExpr] = x match {
            case '{ SqlCaseExpr(${Expr(c)}, ${Expr(d)}) } => Some(SqlCaseExpr(c, d))
            case _ => None
        }
    }
}

case class SqlSubQueryPredicateExpr(select: SqlSelectQueryExpr, predicate: SqlSubQueryPredicate) extends SqlExpr

object SqlSubQueryPredicateExpr {
    given FromExpr[SqlSubQueryPredicateExpr] with {
        override def unapply(x: Expr[SqlSubQueryPredicateExpr])(using Quotes): Option[SqlSubQueryPredicateExpr] = x match {
            case '{ SqlSubQueryPredicateExpr(${Expr(s)}, ${Expr(p)}) } => Some(SqlSubQueryPredicateExpr(s, p))
            case _ => None
        }
    }
}
