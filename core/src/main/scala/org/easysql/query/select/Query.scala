package org.easysql.query.select

import org.easysql.ast.expr.{SqlAllColumnExpr, SqlBinaryExpr, SqlBinaryOperator}
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.order.SqlOrderBy
import org.easysql.dsl.*
import org.easysql.ast.statement.select.{SqlSelect, SqlSelectItem, SqlSelectQuery}
import org.easysql.ast.table.*
import org.easysql.database.DB
import org.easysql.visitor.visitExpr
import org.easysql.util.toSqlString
import org.easysql.macros.columnsMacro

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class Query[T <: Tuple | Expr[_] | TableSchema](t: T) extends SelectQueryImpl[QueryType[T]] {
    private var sqlSelect: SqlSelect = SqlSelect(selectList = ListBuffer(SqlSelectItem(SqlAllColumnExpr())))

    def getSelect: SqlSelectQuery = sqlSelect

    def filter(f: T => Expr[Boolean]): Query[T] = {
        val expr = f(t)
        sqlSelect.addCondition(visitExpr(expr))
        this
    }

    def withFilter(f: T => Expr[Boolean]): Query[T] = filter(f)

    def map[R <: Tuple | Expr[_]](f: T => R): Query[RecursiveInverseMap[QueryType[R], Expr]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }
        spread(f(t))(addItem)
        this.asInstanceOf[Query[RecursiveInverseMap[QueryType[R], Expr]]]
    }

    def flatMap[R <: Tuple | Expr[_]](f: T => Query[R]): Query[RecursiveInverseMap[QueryType[R], Expr]] = {
        val from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.INNER_JOIN, f(t).sqlSelect.from.get))
        val where = if (f(t).sqlSelect.where.nonEmpty) {
            val expr = SqlBinaryExpr(this.sqlSelect.where.get, SqlBinaryOperator.AND, f(t).sqlSelect.where.get)
            Some(expr)
        } else {
            this.sqlSelect.where
        }
        this.sqlSelect = f(t).sqlSelect.copy(from = from, where = where)
        this.asInstanceOf[Query[RecursiveInverseMap[QueryType[R], Expr]]]
    }

    def distinct: Query[T] = {
        this.sqlSelect.distinct = true
        this
    }

    def sortBy[R <: Tuple | OrderBy](f: T => R): Query[T] = {
        spread(f(t))(addSortBy)
        this
    }

    def groupBy[R <: Tuple | Expr[_]](f: T => R): Query[(R, T)] = {
        spread(f(t))(addGroupBy)
        val query = new Query[(R, T)](f(t) -> t)
        query.sqlSelect = this.sqlSelect
        query
    }

    def having(f: T => Expr[Boolean]): Query[T] = {
        val expr = f(t)
        sqlSelect.addHaving(visitExpr(expr))
        this
    }

    def joinInner[JT <: TableSchema](joinTable: JT): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable))
        query.sqlSelect.from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.INNER_JOIN, SqlIdentifierTableSource(joinTable.tableName)))
        query
    }

    def joinLeft[JT <: TableSchema](joinTable: JT): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable))
        query.sqlSelect.from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.LEFT_JOIN, SqlIdentifierTableSource(joinTable.tableName)))
        query
    }

    def joinRight[JT <: TableSchema](joinTable: JT): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable))
        query.sqlSelect.from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.RIGHT_JOIN, SqlIdentifierTableSource(joinTable.tableName)))
        query
    }

    def joinCross[JT <: TableSchema](joinTable: JT): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable))
        query.sqlSelect.from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.CROSS_JOIN, SqlIdentifierTableSource(joinTable.tableName)))
        query
    }

    def joinFull[JT <: TableSchema](joinTable: JT): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable))
        query.sqlSelect.from = Some(SqlJoinTableSource(this.sqlSelect.from.get, SqlJoinType.FULL_JOIN, SqlIdentifierTableSource(joinTable.tableName)))
        query
    }

    def on(f: T => Expr[Boolean]): Query[T] = {
        this.sqlSelect.from.get match {
            case j: SqlJoinTableSource => j.on = Some(visitExpr(f(t)))
            case _ => 
        }
        this
    }

    def take(n: Int): Query[T] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(n, 0))
        } else {
            this.sqlSelect.limit.get.limit = n
        }
        this
    }

    def drop(n: Int): Query[T] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(1, n))
        } else {
            this.sqlSelect.limit.get.offset = n
        }
        this
    }

    private def addGroupBy(column: Expr[_]): Unit = {
        this.sqlSelect.groupBy.append(visitExpr(column))
    }

    private def addItem(column: Expr[_]): Unit = {
        if (column.alias.isEmpty) {
            this.sqlSelect.addSelectItem(visitExpr(column))
        } else {
            this.sqlSelect.addSelectItem(visitExpr(column), column.alias)
        }
    }

    private def addSortBy(sortBy: OrderBy): Unit = {
        val sqlOrderBy = SqlOrderBy(visitExpr(sortBy.query), sortBy.order)
        this.sqlSelect.orderBy.append(sqlOrderBy)
    }

    private def spread[K](items: Any)(handler: K => Unit)(using ClassTag[K]): Unit = {
        items match {
            case tu: Tuple => tu.toArray.foreach(spread(_)(handler))
            case k: K => handler(k)
            case _ =>
        }
    }

    def sql(db: DB): String = toSqlString(this.sqlSelect, db)

    def toSql(using db: DB): String = toSqlString(this.sqlSelect, db)
}

object Query {
    inline def apply[T <: TableSchema](table: T): Query[T] = {
        val query = new Query[T](table)
        query.sqlSelect.from = Some(SqlIdentifierTableSource(table.tableName))
        query
    }
}