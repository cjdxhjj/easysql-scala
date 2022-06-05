package org.easysql.ast.statement.select

import scala.quoted.*

enum SqlUnionType(val unionType: String) {
    case UNION extends SqlUnionType("UNION")
    case UNION_ALL extends SqlUnionType("UNION ALL")
    case EXCEPT extends SqlUnionType("EXCEPT")
    case EXCEPT_ALL extends SqlUnionType("EXCEPT ALL")
    case INTERSECT extends SqlUnionType("INTERSECT")
    case INTERSECT_ALL extends SqlUnionType("INTERSECT ALL")
}

object SqlUnionType {
    given FromExpr[SqlUnionType] with {
        override def unapply(x: Expr[SqlUnionType])(using Quotes): Option[SqlUnionType] = x match {
            case '{ SqlUnionType.UNION } => Some(SqlUnionType.UNION)
            case '{ SqlUnionType.UNION_ALL } => Some(SqlUnionType.UNION_ALL)
            case '{ SqlUnionType.EXCEPT } => Some(SqlUnionType.EXCEPT)
            case '{ SqlUnionType.EXCEPT_ALL } => Some(SqlUnionType.EXCEPT_ALL)
            case '{ SqlUnionType.INTERSECT } => Some(SqlUnionType.INTERSECT)
            case '{ SqlUnionType.INTERSECT_ALL } => Some(SqlUnionType.INTERSECT_ALL)
            case _ => None
        }
    }
}