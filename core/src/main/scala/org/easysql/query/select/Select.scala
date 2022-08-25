package org.easysql.query.select

import org.easysql.ast.expr.{SqlAllColumnExpr, SqlIdentifierExpr}
import org.easysql.ast.limit.SqlLimit
import org.easysql.ast.order.{SqlOrderBy, SqlOrderByOption}
import org.easysql.ast.statement.select.{SqlSelect, SqlSelectItem, SqlSelectQuery}
import org.easysql.ast.table.*
import org.easysql.ast.SqlDataType
import org.easysql.database.DB
import org.easysql.dsl.{JoinTableSchema, TableSchema, *}
import org.easysql.util.toSqlString
import org.easysql.visitor.*

import java.sql.Connection
import scala.collection.mutable.ListBuffer

class Select[T <: Tuple] extends SelectQuery[T] with Selectable {
    private val sqlSelect = SqlSelect(selectList = ListBuffer(SqlSelectItem(SqlAllColumnExpr())))

    private var joinLeft: SqlTableSource = SqlIdentifierTableSource("")

    private val selectItems: ListBuffer[String] = ListBuffer()

    var aliasName: Option[String] = None

    infix def as(name: String)(using NonEmpty[name.type] =:= Any): SelectType[T] = {
        this.aliasName = Some(name)
        this.asInstanceOf[SelectType[T]]
    }

    infix def unsafeAs(name: String): Select[T] = {
        this.aliasName = Some(name)
        this
    }

    infix def from[Table <: TableSchema[_]](table: Table): Select[T] = {
        val from = SqlIdentifierTableSource(table.tableName)
        from.alias = table.aliasName
        joinLeft = from
        sqlSelect.from = Some(from)

        this
    }

    infix def from(table: SelectQuery[_]): Select[T] = {
        val from = SqlSubQueryTableSource(table.getSelect)
        table match {
            case s: Select[_] => from.alias = s.aliasName
        }

        joinLeft = from
        sqlSelect.from = Some(from)

        this
    }

    infix def fromLateral(table: SelectQuery[_]): Select[T] = {
        val from = SqlSubQueryTableSource(table.getSelect, true)
        table match {
            case s: Select[_] => from.alias = s.aliasName
        }

        joinLeft = from
        sqlSelect.from = Some(from)

        this
    }

    infix def select[U <: Tuple](items: U): Select[Tuple.Concat[T, RecursiveInverseMap[U]]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        def addItem(column: Expr[_]): Unit = {
            if (column.alias.isEmpty) {
                sqlSelect.addSelectItem(visitExpr(column))
            } else {
                sqlSelect.addSelectItem(visitExpr(column), column.alias)
            }
            selectItems.append(column.alias.getOrElse(""))
        }

        def spread(items: Tuple): Unit = {
            items.toArray.foreach {
                case t: Tuple => spread(t)
                case expr: Expr[_] => addItem(expr)
            }
        }

        spread(items)

        this.asInstanceOf[Select[Tuple.Concat[T, RecursiveInverseMap[U]]]]
    }

    infix def select[I <: SqlDataType | Null](item: Expr[I]): Select[Tuple.Concat[T, Tuple1[I]]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        if (item.alias.isEmpty) {
            sqlSelect.addSelectItem(visitExpr(item))
        } else {
            sqlSelect.addSelectItem(visitExpr(item), item.alias)
        }
        selectItems.append(item.alias.getOrElse(""))

        this.asInstanceOf[Select[Tuple.Concat[T, Tuple1[I]]]]
    }

    infix def dynamicSelect(columns: Expr[_]*): Select[Tuple1[Nothing]] = {
        if (this.sqlSelect.selectList.size == 1 && this.sqlSelect.selectList.head.expr.isInstanceOf[SqlAllColumnExpr]) {
            this.sqlSelect.selectList.clear()
        }

        columns.foreach { item =>
            if (item.alias.isEmpty) {
                sqlSelect.addSelectItem(visitExpr(item))
            } else {
                sqlSelect.addSelectItem(visitExpr(item), item.alias)
            }
            selectItems.append(item.alias.getOrElse(""))
        }

        this.asInstanceOf[Select[Tuple1[Nothing]]]
    }

    def distinct: Select[T] = {
        sqlSelect.distinct = true
        this
    }

    infix def where(condition: Expr[_]): Select[T] = {
        sqlSelect.addCondition(getExpr(condition))

        this
    }

    def where(test: () => Boolean, condition: Expr[_]): Select[T] = {
        if (test()) {
            sqlSelect.addCondition(getExpr(condition))
        }

        this
    }

    def where(test: Boolean, condition: Expr[_]): Select[T] = {
        if (test) {
            sqlSelect.addCondition(getExpr(condition))
        }

        this
    }

    infix def having(condition: Expr[_]): Select[T] = {
        sqlSelect.addHaving(getExpr(condition))

        this
    }

    infix def orderBy(item: OrderBy*): Select[T] = {
        val sqlOrderBy = item.map(o => SqlOrderBy(visitExpr(o.query), o.order))
        this.sqlSelect.orderBy.addAll(sqlOrderBy)

        this
    }

    infix def limit(count: Int): Select[T] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(count, 0))
        } else {
            this.sqlSelect.limit.get.limit = count
        }
        this
    }

    infix def offset(offset: Int): Select[T] = {
        if (this.sqlSelect.limit.isEmpty) {
            this.sqlSelect.limit = Some(SqlLimit(1, offset))
        } else {
            this.sqlSelect.limit.get.offset = offset
        }
        this
    }

    infix def groupBy(item: Expr[_]*): Select[T] = {
        this.sqlSelect.groupBy.appendAll(item.map(i => visitExpr(i)))

        this
    }

    private def joinClause(table: TableSchema[_], joinType: SqlJoinType): Select[T] = {
        val joinTable = SqlIdentifierTableSource(table.tableName)
        joinTable.alias = table.aliasName
        val join = SqlJoinTableSource(joinLeft, joinType, joinTable)
        sqlSelect.from = Some(join)
        joinLeft = join

        this
    }

    private def joinClause(table: SelectQuery[_], joinType: SqlJoinType, isLateral: Boolean = false): Select[T] = {
        val join = SqlJoinTableSource(joinLeft, joinType, SqlSubQueryTableSource(table.getSelect, isLateral = isLateral))
        table match {
            case s: Select[_] => join.alias = s.aliasName
        }

        sqlSelect.from = Some(join)
        joinLeft = join

        this
    }

    private def joinClause(table: JoinTableSchema, joinType: SqlJoinType): Select[T] = {
        def unapplyTable(t: AnyTable): SqlTableSource = {
            t match {
                case table: TableSchema[_] =>
                    val ts = SqlIdentifierTableSource(table.tableName)
                    ts.alias = table.aliasName
                    ts
                case j: JoinTableSchema => SqlJoinTableSource(unapplyTable(j.left), j.joinType, unapplyTable(j.right), j.onCondition.map(getExpr))
            }
        }

        val joinTableSource = SqlJoinTableSource(unapplyTable(table.left), table.joinType, unapplyTable(table.right), table.onCondition.map(getExpr))
        val join = SqlJoinTableSource(joinLeft, joinType, joinTableSource)

        sqlSelect.from = Some(join)
        joinLeft = join

        this
    }

    infix def on(onCondition: Expr[_]): Select[T] = {
        val from = this.sqlSelect.from.get
        from match {
            case table: SqlJoinTableSource => table.on = Some(visitExpr(onCondition))
            case _ =>
        }

        this
    }

    infix def join(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.JOIN)

    infix def join(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.JOIN)

    infix def join(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.JOIN)

    infix def joinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.JOIN, true)

    infix def leftJoin(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.LEFT_JOIN)

    infix def leftJoin(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.LEFT_JOIN)

    infix def leftJoin(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.LEFT_JOIN)

    infix def leftJoinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.LEFT_JOIN, true)

    infix def rightJoin(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.RIGHT_JOIN)

    infix def rightJoin(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.RIGHT_JOIN)

    infix def rightJoin(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.RIGHT_JOIN)

    infix def rightJoinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.RIGHT_JOIN, true)

    infix def innerJoin(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.INNER_JOIN)

    infix def innerJoin(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.INNER_JOIN)

    infix def innerJoin(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.INNER_JOIN)

    infix def innerJoinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.INNER_JOIN, true)

    infix def crossJoin(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.CROSS_JOIN)

    infix def crossJoin(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.CROSS_JOIN)

    infix def crossJoin(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.CROSS_JOIN)

    infix def crossJoinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.CROSS_JOIN, true)

    infix def fullJoin(table: TableSchema[_]): Select[T] = joinClause(table, SqlJoinType.FULL_JOIN)

    infix def fullJoin(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.FULL_JOIN)

    infix def fullJoin(table: JoinTableSchema): Select[T] = joinClause(table, SqlJoinType.FULL_JOIN)

    infix def fullJoinLateral(query: SelectQuery[_]): Select[T] = joinClause(query, SqlJoinType.FULL_JOIN, true)

    def forUpdate: Select[T] = {
        this.sqlSelect.forUpdate = true
        this
    }

    override def getSelect: SqlSelectQuery = sqlSelect

    override def sql(db: DB): String = toSqlString(sqlSelect, db)

    override def toSql(using db: DB): String = toSqlString(sqlSelect, db)

    def countSql(db: DB): String = {
        val selectCopy = this.sqlSelect.copy(selectList = ListBuffer(), limit = None, orderBy = ListBuffer())
        selectCopy.selectList.clear()
        selectCopy.orderBy.clear()
        selectCopy.addSelectItem(visitExpr(count()), Some("count"))

        toSqlString(selectCopy, db)
    }

    def toCountSql(using db: DB): String = countSql(db)

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

    def selectDynamic(name: String): Any = name match {
        case "_1" => col(s"${aliasName.getOrElse("")}.${selectItems.head}")
        case "_2" => col(s"${aliasName.getOrElse("")}.${selectItems(1)}")
        case "_3" => col(s"${aliasName.getOrElse("")}.${selectItems(2)}")
        case "_4" => col(s"${aliasName.getOrElse("")}.${selectItems(3)}")
        case "_5" => col(s"${aliasName.getOrElse("")}.${selectItems(4)}")
        case "_6" => col(s"${aliasName.getOrElse("")}.${selectItems(5)}")
        case "_7" => col(s"${aliasName.getOrElse("")}.${selectItems(6)}")
        case "_8" => col(s"${aliasName.getOrElse("")}.${selectItems(7)}")
        case "_9" => col(s"${aliasName.getOrElse("")}.${selectItems(8)}")
        case "_10" => col(s"${aliasName.getOrElse("")}.${selectItems(9)}")
        case "_11" => col(s"${aliasName.getOrElse("")}.${selectItems(10)}")
        case "_12" => col(s"${aliasName.getOrElse("")}.${selectItems(11)}")
        case "_13" => col(s"${aliasName.getOrElse("")}.${selectItems(12)}")
        case "_14" => col(s"${aliasName.getOrElse("")}.${selectItems(13)}")
        case "_15" => col(s"${aliasName.getOrElse("")}.${selectItems(14)}")
        case "_16" => col(s"${aliasName.getOrElse("")}.${selectItems(15)}")
        case "_17" => col(s"${aliasName.getOrElse("")}.${selectItems(16)}")
        case "_18" => col(s"${aliasName.getOrElse("")}.${selectItems(17)}")
        case "_19" => col(s"${aliasName.getOrElse("")}.${selectItems(18)}")
        case "_20" => col(s"${aliasName.getOrElse("")}.${selectItems(19)}")
        case "_21" => col(s"${aliasName.getOrElse("")}.${selectItems(20)}")
        case "_22" => col(s"${aliasName.getOrElse("")}.${selectItems(21)}")
        case _ => col[SqlDataType | Null](s"${aliasName.get}.$name")
    }
}

object Select {
    def apply(): Select[EmptyTuple] = new Select()
}