package org.easysql.ast.expr

import scala.quoted.*

enum SqlSubQueryPredicate(val predicate: String) {
    case EXISTS extends SqlSubQueryPredicate("EXISTS")
    case NOT_EXISTS extends SqlSubQueryPredicate("NOT EXISTS")
    case ANY extends SqlSubQueryPredicate("ANY")
    case ALL extends SqlSubQueryPredicate("ALL")
    case SOME extends SqlSubQueryPredicate("SOME")
}

object SqlSubQueryPredicate {
    given FromExpr[SqlSubQueryPredicate] with {
        override def unapply(x: Expr[SqlSubQueryPredicate])(using Quotes): Option[SqlSubQueryPredicate] = x match {
            case '{ SqlSubQueryPredicate.EXISTS } => Some(SqlSubQueryPredicate.EXISTS)
            case '{ SqlSubQueryPredicate.NOT_EXISTS } => Some(SqlSubQueryPredicate.NOT_EXISTS)
            case '{ SqlSubQueryPredicate.ANY } => Some(SqlSubQueryPredicate.ANY)
            case '{ SqlSubQueryPredicate.ALL } => Some(SqlSubQueryPredicate.ALL)
            case '{ SqlSubQueryPredicate.SOME } => Some(SqlSubQueryPredicate.SOME)
            case _ => None
        }
    }
}