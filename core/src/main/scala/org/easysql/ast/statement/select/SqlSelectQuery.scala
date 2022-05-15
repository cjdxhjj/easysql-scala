package org.easysql.ast.statement.select

import org.easysql.ast.expr._
import org.easysql.ast.order.SqlOrderBy
import org.easysql.ast.statement.SqlStatement
import org.easysql.ast.table.SqlTableSource
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.statement.select._

import scala.collection.mutable.ListBuffer

sealed class SqlSelectQuery extends SqlStatement

case class SqlSelect(var distinct: Boolean = false,
                     selectList: ListBuffer[SqlSelectItem] = ListBuffer(),
                     var from: Option[SqlTableSource] = None,
                     var where: Option[SqlExpr] = None,
                     groupBy: ListBuffer[SqlExpr] = ListBuffer(),
                     orderBy: ListBuffer[SqlOrderBy] = ListBuffer(),
                     var forUpdate: Boolean = false,
                     var limit: Option[SqlLimit] = None,
                     var having: Option[SqlExpr] = None
                    ) extends SqlSelectQuery() {
    def addSelectItem(expr: SqlExpr | String, alias: Option[String] = None): Unit = {
        val sqlExpr = expr match {
            case e: SqlExpr => e
            case s: String => SqlIdentifierExpr(s)
        }
        selectList += SqlSelectItem(sqlExpr, alias)
    }

    def addCondition(condition: SqlExpr): Unit = {
        where = if (where.isEmpty) {
            Some(condition)
        } else  {
            Some(SqlBinaryExpr(where.get, SqlBinaryOperator.AND, condition))
        }
    }

    def addHaving(condition: SqlExpr): Unit = {
        having = if (having.isEmpty) {
            Some(condition)
        } else  {
            Some(SqlBinaryExpr(having.get, SqlBinaryOperator.AND, condition))
        }
    }
}

case class SqlUnionSelect(left: SqlSelectQuery, unionType: SqlUnionType, right: SqlSelectQuery) extends SqlSelectQuery()

case class SqlWithSelect(withList: ListBuffer[SqlWithItem] = ListBuffer(), var recursive: Boolean = false, var query: Option[SqlSelectQuery] = None) extends SqlSelectQuery()

case class SqlValuesSelect(values: ListBuffer[List[SqlExpr]] = ListBuffer()) extends SqlSelectQuery()