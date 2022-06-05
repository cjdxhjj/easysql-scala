package org.easysql.ast.expr

import scala.quoted.*

enum SqlBinaryOperator(val operator: String) {
    case IS extends SqlBinaryOperator("IS")
    case IS_NOT extends SqlBinaryOperator("IS NOT")
    case EQ extends SqlBinaryOperator("=")
    case NE extends SqlBinaryOperator("<>")
    case LIKE extends SqlBinaryOperator("LIKE")
    case NOT_LIKE extends SqlBinaryOperator("NOT LIKE")
    case GT extends SqlBinaryOperator(">")
    case GE extends SqlBinaryOperator(">=")
    case LT extends SqlBinaryOperator("<")
    case LE extends SqlBinaryOperator("<=")
    case AND extends SqlBinaryOperator("AND")
    case OR extends SqlBinaryOperator("OR")
    case XOR extends SqlBinaryOperator("XOR")
    case ADD extends SqlBinaryOperator("+")
    case SUB extends SqlBinaryOperator("-")
    case MUL extends SqlBinaryOperator("*")
    case DIV extends SqlBinaryOperator("/")
    case MOD extends SqlBinaryOperator("%")
    case SUB_GT extends SqlBinaryOperator("->")
    case SUB_GT_GT extends SqlBinaryOperator("->>")
    case CONCAT extends SqlBinaryOperator("||")
}

object SqlBinaryOperator {
    given FromExpr[SqlBinaryOperator] with {
        override def unapply(x: Expr[SqlBinaryOperator])(using Quotes): Option[SqlBinaryOperator] = x match {
            case '{ SqlBinaryOperator.IS } => Some(SqlBinaryOperator.IS)
            case '{ SqlBinaryOperator.IS_NOT } => Some(SqlBinaryOperator.IS_NOT)
            case '{ SqlBinaryOperator.EQ } => Some(SqlBinaryOperator.EQ)
            case '{ SqlBinaryOperator.NE } => Some(SqlBinaryOperator.NE)
            case '{ SqlBinaryOperator.LIKE } => Some(SqlBinaryOperator.LIKE)
            case '{ SqlBinaryOperator.NOT_LIKE } => Some(SqlBinaryOperator.NOT_LIKE)
            case '{ SqlBinaryOperator.GT } => Some(SqlBinaryOperator.GT)
            case '{ SqlBinaryOperator.GE } => Some(SqlBinaryOperator.GE)
            case '{ SqlBinaryOperator.LT } => Some(SqlBinaryOperator.LT)
            case '{ SqlBinaryOperator.LE } => Some(SqlBinaryOperator.LE)
            case '{ SqlBinaryOperator.AND } => Some(SqlBinaryOperator.AND)
            case '{ SqlBinaryOperator.OR } => Some(SqlBinaryOperator.OR)
            case '{ SqlBinaryOperator.XOR } => Some(SqlBinaryOperator.XOR)
            case '{ SqlBinaryOperator.ADD } => Some(SqlBinaryOperator.ADD)
            case '{ SqlBinaryOperator.SUB } => Some(SqlBinaryOperator.SUB)
            case '{ SqlBinaryOperator.MUL } => Some(SqlBinaryOperator.MUL)
            case '{ SqlBinaryOperator.DIV } => Some(SqlBinaryOperator.DIV)
            case '{ SqlBinaryOperator.MOD } => Some(SqlBinaryOperator.MOD)
            case '{ SqlBinaryOperator.SUB_GT } => Some(SqlBinaryOperator.SUB_GT)
            case '{ SqlBinaryOperator.SUB_GT_GT } => Some(SqlBinaryOperator.SUB_GT_GT)
            case '{ SqlBinaryOperator.CONCAT } => Some(SqlBinaryOperator.CONCAT)
            case _ => None
        }
    }
}