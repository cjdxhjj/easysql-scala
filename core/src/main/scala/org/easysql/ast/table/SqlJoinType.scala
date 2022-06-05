package org.easysql.ast.table

import scala.quoted.*

enum SqlJoinType(val joinType: String) {
    case JOIN extends SqlJoinType("JOIN")
    case INNER_JOIN extends SqlJoinType("INNER JOIN")
    case LEFT_JOIN extends SqlJoinType("LEFT JOIN")
    case RIGHT_JOIN extends SqlJoinType("RIGHT JOIN")
    case FULL_JOIN extends SqlJoinType("FULL JOIN")
    case CROSS_JOIN extends SqlJoinType("CROSS JOIN")
}

object SqlJoinType {
    given FromExpr[SqlJoinType] with {
        override def unapply(x: Expr[SqlJoinType])(using Quotes): Option[SqlJoinType] = x match {
            case '{ SqlJoinType.JOIN } => Some(SqlJoinType.JOIN)
            case '{ SqlJoinType.INNER_JOIN } => Some(SqlJoinType.INNER_JOIN)
            case '{ SqlJoinType.LEFT_JOIN } => Some(SqlJoinType.LEFT_JOIN)
            case '{ SqlJoinType.RIGHT_JOIN } => Some(SqlJoinType.RIGHT_JOIN)
            case '{ SqlJoinType.FULL_JOIN } => Some(SqlJoinType.FULL_JOIN)
            case '{ SqlJoinType.CROSS_JOIN } => Some(SqlJoinType.CROSS_JOIN)
            case _ => None
        }
    }
}