package org.easysql.ast.statement.update

import org.easysql.ast.expr.{SqlBinaryExpr, SqlBinaryOperator, SqlExpr}
import org.easysql.ast.statement.SqlStatement

import scala.collection.mutable.ListBuffer

case class SqlUpdate(var table: Option[SqlExpr] = None,
                     setList: ListBuffer[(SqlExpr, SqlExpr)] = ListBuffer(),
                     var where: Option[SqlExpr] = None) extends SqlStatement {
    def addCondition(condition: SqlExpr): Unit = {
        where = if (where.isEmpty) {
            Some(condition)
        } else {
            Some(SqlBinaryExpr(where.get, SqlBinaryOperator.AND, condition))
        }
    }
}