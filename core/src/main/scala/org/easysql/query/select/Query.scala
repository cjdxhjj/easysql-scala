package org.easysql.query.select

import org.easysql.ast.expr.{SqlAllColumnExpr, SqlBinaryExpr, SqlBinaryOperator}
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.order.SqlOrderBy
import org.easysql.dsl.{Expr, *}
import org.easysql.ast.statement.select.{SqlSelect, SqlSelectItem, SqlSelectQuery}
import org.easysql.ast.table.*
import org.easysql.database.DB
import org.easysql.visitor.visitExpr
import org.easysql.util.toSqlString
import org.easysql.macros.columnsMacro

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class Query[T <: Tuple | Expr[_] | TableSchema](t: T, var ast: SqlSelect) extends SelectQueryImpl[QueryType[T]] {
    inline def getSelect: SqlSelect = ast

    def filter(f: T => Expr[Boolean]): Query[T] = {
        val expr = f(t)
        ast.addCondition(visitExpr(expr))
        this
    }

    def withFilter(f: T => Expr[Boolean]): Query[T] = filter(f)

    inline def map[R <: Tuple | Expr[_]](f: T => R): Query[RecursiveInverseMap[QueryType[R], Expr]] = {
        if (ast.selectList.size == 1 && ast.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            ast.selectList = List()
        }
        spread(f(t))(addItem)
        this.asInstanceOf[Query[RecursiveInverseMap[QueryType[R], Expr]]]
    }

    def flatMap[R <: Tuple | Expr[_]](f: T => Query[R]): Query[R] = {
        val join = f(t).ast.from.get
        val from = Some(SqlJoinTableSource(ast.from.get, SqlJoinType.INNER_JOIN, join, None, None, List()))
        val where = if (f(t).ast.where.nonEmpty) {
            val expr = SqlBinaryExpr(ast.where.get, SqlBinaryOperator.AND, f(t).ast.where.get)
            Some(expr)
        } else {
            ast.where
        }
        ast = f(t).ast.copy(from = from, where = where)
        this.asInstanceOf[Query[R]]
    }

    def distinct: Query[T] = {
        ast.distinct = true
        this
    }

    def sortBy[R <: Tuple | OrderBy](f: T => R): Query[T] = {
        spread(f(t))(addSortBy)
        this
    }

    def groupBy[R <: Tuple | Expr[_]](f: T => R): Query[(R, T)] = {
        spread(f(t))(addGroupBy)
        val query = new Query[(R, T)](f(t) -> t, ast)
        query
    }

    def having(f: T => Expr[Boolean]): Query[T] = {
        val expr = f(t)
        ast.addHaving(visitExpr(expr))
        this
    }

    private def join[JT <: TableSchema | AliasedTableSchema](joinTable: JT, joinType: SqlJoinType): Query[(T, JT)] = {
        val query = new Query[(T, JT)]((t, joinTable), ast)
        val join = joinTable match {
            case t: TableSchema => SqlIdentifierTableSource(t.tableName, None, List())
            case a: AliasedTableSchema => {
                val table = SqlIdentifierTableSource(a.tableName, Some(a.aliasName), List())
                table
            }
        }
        query.ast.from = Some(SqlJoinTableSource(ast.from.get, SqlJoinType.INNER_JOIN, join, None, None, List()))
        query
    }

    def joinInner[JT <: TableSchema | AliasedTableSchema](joinTable: JT): Query[(T, JT)] = join(joinTable, SqlJoinType.INNER_JOIN)

    def joinLeft[JT <: TableSchema | AliasedTableSchema](joinTable: JT): Query[(T, JT)] = join(joinTable, SqlJoinType.LEFT_JOIN)

    def joinRight[JT <: TableSchema | AliasedTableSchema](joinTable: JT): Query[(T, JT)] = join(joinTable, SqlJoinType.RIGHT_JOIN)

    def joinCross[JT <: TableSchema | AliasedTableSchema](joinTable: JT): Query[(T, JT)] = join(joinTable, SqlJoinType.CROSS_JOIN)

    def joinFull[JT <: TableSchema | AliasedTableSchema](joinTable: JT): Query[(T, JT)] = join(joinTable, SqlJoinType.FULL_JOIN)

    def on(f: T => Expr[Boolean]): Query[T] = {
        ast.from.get match {
            case j: SqlJoinTableSource => j.on = Some(visitExpr(f(t)))
            case _ => 
        }
        this
    }

    def take(n: Int): Query[T] = {
        if (ast.limit.isEmpty) {
            ast.limit = Some(SqlLimit(n, 0))
        } else {
            ast.limit.get.limit = n
        }
        this
    }

    def drop(n: Int): Query[T] = {
        if (ast.limit.isEmpty) {
            ast.limit = Some(SqlLimit(1, n))
        } else {
            ast.limit.get.offset = n
        }
        this
    }

    private def addGroupBy(column: Expr[_]): Unit = {
        ast.groupBy = ast.groupBy ::: List(visitExpr(column))
    }

    private def addItem(column: Expr[_]): Unit = {
        if (column.alias.isEmpty) {
            ast.addSelectItem(visitExpr(column))
        } else {
            ast.addSelectItem(visitExpr(column), column.alias)
        }
    }

    private def addSortBy(sortBy: OrderBy): Unit = {
        val sqlOrderBy = SqlOrderBy(visitExpr(sortBy.query), sortBy.order)
        ast.orderBy = ast.orderBy ::: List(sqlOrderBy)
    }

    private def spread[K](items: Any)(handler: K => Unit)(using ClassTag[K]): Unit = {
        items match {
            case tu: Tuple => tu.toArray.foreach(spread(_)(handler))
            case k: K => handler(k)
            case _ =>
        }
    }

    def sql(db: DB): String = toSqlString(ast, db)

    def toSql(using db: DB): String = toSqlString(ast, db)
}

object Query {
    inline def apply[T <: TableSchema](table: T): Query[T] = {
        val query = new Query[T](table, SqlSelect(false, List(SqlSelectItem(SqlAllColumnExpr(None), None)), Some(SqlIdentifierTableSource(table.tableName, None, List())), None, List(), List(), false, None, None))
        query
    }
}