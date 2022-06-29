package org.easysql.query.select

import jdk.jfr.Experimental
import org.easysql.ast.expr.{SqlAllColumnExpr, SqlIdentifierExpr}
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.order.{SqlOrderBy, SqlOrderByOption}
import org.easysql.ast.statement.select.{SqlSelect, SqlSelectItem, SqlSelectQuery}
import org.easysql.ast.table.*
import org.easysql.ast.SqlSingleConstType
import org.easysql.database.DB
import org.easysql.dsl.{JoinTableSchema, TableSchema, *}
import org.easysql.util.toSqlString
import org.easysql.visitor.*

import java.sql.Connection
import scala.annotation.experimental
import scala.collection.mutable.ListBuffer
import scala.language.dynamics

class Select[T <: Tuple, FromTables <: Tuple, QuoteTables <: Tuple] extends SelectQueryImpl[T] with Dynamic {
    private val sqlSelect = SqlSelect(selectList = ListBuffer(SqlSelectItem(SqlAllColumnExpr())))

    private var joinLeft: SqlTableSource = SqlIdentifierTableSource("")

    private var aliasName: Option[String] = None

    infix def from[Table <: TableSchema](table: Table): Select[T, Tuple1[Table], QuoteTables] = {
        val from = Some(SqlIdentifierTableSource(table.tableName))
        joinLeft = from.get
        sqlSelect.from = from

        this.asInstanceOf[Select[T, Tuple1[Table], QuoteTables]]
    }

    infix def from(table: AliasedTableSchema | String): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val tableName = table match {
            case aliasedTableSchema: AliasedTableSchema => aliasedTableSchema.tableName
            case string: String => string
        }

        val from = Some(SqlIdentifierTableSource(tableName))
        table match
            case a: AliasedTableSchema => from.get.alias = Some(a.aliasName)
            case _ =>
        joinLeft = from.get
        sqlSelect.from = from

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def from(table: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val from = Some(SqlSubQueryTableSource(table.getSelect))
        joinLeft = from.get
        sqlSelect.from = from

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def from(table: Select[_, _, _]): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val from = SqlSubQueryTableSource(table.getSelect)
        if (table.aliasName.nonEmpty) {
            from.alias = table.aliasName
        }
        joinLeft = from
        sqlSelect.from = Some(from)

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def fromLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val from = Some(SqlSubQueryTableSource(query.getSelect, true))
        joinLeft = from.get
        sqlSelect.from = from

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def fromLateral(table: Select[_, _, _]): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val from = SqlSubQueryTableSource(table.getSelect, true)
        if (table.aliasName.nonEmpty) {
            from.alias = table.aliasName
        }
        joinLeft = from
        sqlSelect.from = Some(from)

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def as(name: String)(using NonEmpty[name.type] =:= Any): Select[T, FromTables, QuoteTables] = {
        this.aliasName = Some(name)
        this
    }

    infix def unsafeAs(name: String): Select[T, FromTables, QuoteTables] = {
        this.aliasName = Some(name)
        this
    }

    infix def select[U <: Tuple](items: U): Select[Tuple.Concat[T, RecursiveInverseMap[U]], FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        def addItem(column: Expr[_, _]): Unit = {
            if (column.alias.isEmpty) {
                sqlSelect.addSelectItem(visitExpr(column))
            } else {
                sqlSelect.addSelectItem(visitExpr(column), column.alias)
            }
        }

        def spread(items: Tuple): Unit = {
            items.toArray.foreach {
                case t: Tuple => spread(t)
                case expr: Expr[_, _] => addItem(expr)
            }
        }

        spread(items)

        this.asInstanceOf[Select[Tuple.Concat[T, RecursiveInverseMap[U]], FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]]]
    }

    infix def select[I <: SqlSingleConstType | Null, Q <: Tuple](item: Expr[I, Q]): Select[Tuple.Concat[T, InverseMap[Tuple1[item.type]]], FromTables, Tuple.Concat[QuoteTables, Q]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        if (item.alias.isEmpty) {
            sqlSelect.addSelectItem(visitExpr(item))
        } else {
            sqlSelect.addSelectItem(visitExpr(item), item.alias)
        }

        this.asInstanceOf[Select[Tuple.Concat[T, InverseMap[Tuple1[item.type]]], FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def dynamicSelect(columns: Expr[_, _]*): Select[Tuple1[Nothing], FromTables, EmptyTuple] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        columns.foreach { item =>
            if (item.alias.isEmpty) {
                sqlSelect.addSelectItem(visitExpr(item))
            } else {
                sqlSelect.addSelectItem(visitExpr(item), item.alias)
            }
        }

        this.asInstanceOf[Select[Tuple1[Nothing], FromTables, EmptyTuple]]
    }

    def distinct: Select[T, FromTables, QuoteTables] = {
        sqlSelect.distinct = true
        this
    }

    infix def where[Q <: Tuple](condition: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        sqlSelect.addCondition(getExpr(condition))

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    def where[Q <: Tuple](test: () => Boolean, condition: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        if (test()) {
            sqlSelect.addCondition(getExpr(condition))
        }

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    def where[Q <: Tuple](test: Boolean, condition: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        if (test) {
            sqlSelect.addCondition(getExpr(condition))
        }

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def having[Q <: Tuple](condition: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        sqlSelect.addHaving(getExpr(condition))

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def orderBy[Q <: Tuple](item: OrderBy[Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        val sqlOrderBy = SqlOrderBy(visitExpr(item.query), item.order)
        this.sqlSelect.orderBy.append(sqlOrderBy)

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def orderBy(items: Tuple): Select[T, FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]] = {
        items.toArray.foreach { it =>
            val order = it match
                case o: OrderBy[_] => SqlOrderBy(visitExpr(o.query), o.order)
                case e: Expr[_, _] => SqlOrderBy(visitExpr(e), SqlOrderByOption.ASC)

            this.sqlSelect.orderBy.append(order)
        }

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]]]
    }

    infix def limit(count: Int): Select[T, FromTables, QuoteTables] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(count, 0))
        } else {
            this.sqlSelect.limit.get.limit = count
        }
        this
    }

    infix def offset(offset: Int): Select[T, FromTables, QuoteTables] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(1, offset))
        } else {
            this.sqlSelect.limit.get.offset = offset
        }
        this
    }

    infix def groupBy[Q <: Tuple](item: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        this.sqlSelect.groupBy.append(visitExpr(item))

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def groupBy(items: Tuple): Select[T, FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]] = {
        items.toArray.foreach { it =>
            it match
                case e: Expr[_, _] => this.sqlSelect.groupBy.append(visitExpr(e))
        }

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, FlatTables[QueryQuoteTables[items.type]]]]]
    }

    private def joinClause(table: TableSchema, joinType: SqlJoinType): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = {
        val joinTable = SqlIdentifierTableSource(table.tableName)
        val join = SqlJoinTableSource(joinLeft, joinType, joinTable)
        sqlSelect.from = Some(join)
        joinLeft = join

        this.asInstanceOf[Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables]]
    }

    private def joinClause(table: String | AliasedTableSchema, joinType: SqlJoinType): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val tableName = table match {
            case string: String => string
            case aliasedTableSchema: AliasedTableSchema => aliasedTableSchema.tableName
        }
        val joinTable = SqlIdentifierTableSource(tableName)
        table match
            case a: AliasedTableSchema => joinTable.alias = Some(a.aliasName)
            case _ =>

        val join = SqlJoinTableSource(joinLeft, joinType, joinTable)
        sqlSelect.from = Some(join)
        joinLeft = join

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    private def joinClause(table: SelectQuery[_], joinType: SqlJoinType, isLateral: Boolean = false): Select[T, Tuple1[AnyTable], QuoteTables] = {
        val join = SqlJoinTableSource(joinLeft, joinType, SqlSubQueryTableSource(table.getSelect, isLateral = isLateral))
        table match
            case s: Select[_, _, _] => join.alias = s.aliasName
            case _ =>
        sqlSelect.from = Some(join)
        joinLeft = join

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    private def joinClause(table: JoinTableSchema, joinType: SqlJoinType): Select[T, Tuple1[AnyTable], QuoteTables] = {
        def unapplyTable(t: TableSchema | JoinTableSchema | AliasedTableSchema): SqlTableSource = {
            t match {
                case table: TableSchema => SqlIdentifierTableSource(table.tableName)
                case a: AliasedTableSchema =>
                    val ts = SqlIdentifierTableSource(a.tableName)
                    ts.alias = Some(a.aliasName)
                    ts
                case j: JoinTableSchema => SqlJoinTableSource(unapplyTable(j.left), j.joinType, unapplyTable(j.right), j.onCondition.map(getExpr))
            }
        }

        val joinTableSource = SqlJoinTableSource(unapplyTable(table.left), table.joinType, unapplyTable(table.right), table.onCondition.map(getExpr))
        val join = SqlJoinTableSource(joinLeft, joinType, joinTableSource)

        sqlSelect.from = Some(join)
        joinLeft = join

        this.asInstanceOf[Select[T, Tuple1[AnyTable], QuoteTables]]
    }

    infix def on[Q <: Tuple](onCondition: Expr[_, Q]): Select[T, FromTables, Tuple.Concat[QuoteTables, Q]] = {
        val from = this.sqlSelect.from.get
        from match {
            case table: SqlJoinTableSource => table.on = Some(visitExpr(onCondition))
            case _ =>
        }

        this.asInstanceOf[Select[T, FromTables, Tuple.Concat[QuoteTables, Q]]]
    }

    infix def join(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.JOIN)

    infix def join(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.JOIN)

    infix def join(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.JOIN)

    infix def join(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.JOIN)

    infix def joinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.JOIN, true)

    infix def leftJoin(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.LEFT_JOIN)

    infix def leftJoin(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.LEFT_JOIN)

    infix def leftJoin(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.LEFT_JOIN)

    infix def leftJoin(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.LEFT_JOIN)

    infix def leftJoinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.LEFT_JOIN, true)

    infix def rightJoin(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.RIGHT_JOIN)

    infix def rightJoin(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.RIGHT_JOIN)

    infix def rightJoin(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.RIGHT_JOIN)

    infix def rightJoin(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.RIGHT_JOIN)

    infix def rightJoinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.RIGHT_JOIN, true)

    infix def innerJoin(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.INNER_JOIN)

    infix def innerJoin(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.INNER_JOIN)

    infix def innerJoin(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.INNER_JOIN)

    infix def innerJoin(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.INNER_JOIN)

    infix def innerJoinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.INNER_JOIN, true)

    infix def crossJoin(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.CROSS_JOIN)

    infix def crossJoin(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.CROSS_JOIN)

    infix def crossJoin(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.CROSS_JOIN)

    infix def crossJoin(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.CROSS_JOIN)

    infix def crossJoinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.CROSS_JOIN, true)

    infix def fullJoin(table: TableSchema): Select[T, Tuple.Concat[FromTables, Tuple1[table.type]], QuoteTables] = joinClause(table, SqlJoinType.FULL_JOIN)

    infix def fullJoin(table: String | AliasedTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.FULL_JOIN)

    infix def fullJoin(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.FULL_JOIN)

    infix def fullJoin(table: JoinTableSchema): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(table, SqlJoinType.FULL_JOIN)

    infix def fullJoinLateral(query: SelectQuery[_]): Select[T, Tuple1[AnyTable], QuoteTables] = joinClause(query, SqlJoinType.FULL_JOIN, true)

    def forUpdate: Select[T, FromTables, QuoteTables] = {
        this.sqlSelect.forUpdate = true
        this
    }

    override def getSelect: SqlSelectQuery = sqlSelect

    override def sql(db: DB): String = toSqlString(sqlSelect, db)

    override def toSql(using db: DB): String = toSqlString(sqlSelect, db)

    def asSql(using db: DB)(using QuoteInFrom[QuoteTables, FromTables] =:= Any): String = toSqlString(sqlSelect, db)

    def fetchCountSql(db: DB): String = {
        val selectCopy = this.sqlSelect.copy(selectList = ListBuffer(), limit = None, orderBy = ListBuffer())
        selectCopy.selectList.clear()
        selectCopy.orderBy.clear()
        selectCopy.addSelectItem(visitExpr(count()), Some("count"))

        toSqlString(selectCopy, db)
    }

    def toFetchCountSql(using db: DB): String = fetchCountSql(db)

    def pageSql(pageSize: Int, pageNumber: Int)(db: DB): String = {
        val offset = if (pageNumber <= 1) {
            0
        } else {
            pageSize * (pageNumber - 1)
        }
        val limit = SqlLimit(pageSize, offset)

        val selectCopy = this.sqlSelect.copy(limit = Some(limit))

        toSqlString(selectCopy, db)
    }

    def toPageSql(pageSize: Int, pageNumber: Int)(using db: DB): String = pageSql(pageSize, pageNumber)(db)

    def selectDynamic(name: String): Expr[Nothing, EmptyTuple] = ColumnExpr[Nothing](s"${aliasName.get}.$name")
}

object Select {
    def apply(): Select[EmptyTuple, EmptyTuple, EmptyTuple] = new Select()
}