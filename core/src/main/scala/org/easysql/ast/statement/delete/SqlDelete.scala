package org.easysql.ast.statement.delete

import org.easysql.ast.expr.{SqlBinaryExpr, SqlBinaryOperator, SqlExpr}
import org.easysql.ast.table.SqlIdentTable
import org.easysql.ast.statement.SqlStatement

case class SqlDelete(var table: Option[SqlIdentTable] = None, var where: Option[SqlExpr] = None) extends SqlStatement {
    def addCondition(condition: SqlExpr): Unit = {
        where = if (where.isEmpty) {
            Some(condition)
        } else  {
            Some(SqlBinaryExpr(where.get, SqlBinaryOperator.AND, condition))
        }
    }
}
