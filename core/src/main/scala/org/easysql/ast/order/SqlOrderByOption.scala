package org.easysql.ast.order

import scala.quoted.*

enum SqlOrderByOption(val order: String) {
    case ASC extends SqlOrderByOption("ASC")
    case DESC extends SqlOrderByOption("DESC")

    def turn(): SqlOrderByOption = if (this == ASC) DESC else ASC
}

object SqlOrderByOption {
    given FromExpr[SqlOrderByOption] with {
        override def unapply(x: Expr[SqlOrderByOption])(using Quotes): Option[SqlOrderByOption] = x match {
            case '{ SqlOrderByOption.ASC } => Some(SqlOrderByOption.ASC)
            case '{ SqlOrderByOption.DESC } => Some(SqlOrderByOption.DESC)
            case _ => None
        }
    }
}
