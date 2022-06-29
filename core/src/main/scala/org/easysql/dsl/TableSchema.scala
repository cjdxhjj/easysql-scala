package org.easysql.dsl

import org.easysql.database.TableEntity
import org.easysql.dsl.{AllColumnExpr, TableColumnExpr}
import org.easysql.ast.SqlSingleConstType
import org.easysql.ast.table.SqlJoinType
import org.easysql.macros.columnsMacro
import org.easysql.query.select.{SelectQuery, Query}

import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.language.dynamics

trait AnyTable

trait TableSchema extends AnyTable { self =>
    val tableName: String

    var $columns: ListBuffer[TableColumnExpr[?]] = ListBuffer[TableColumnExpr[?]]()

    def column[T <: SqlSingleConstType](name: String): TableColumnExpr[T] {
        type QuoteTables = Tuple1[self.type]
    } = {
        val c = new TableColumnExpr[T](tableName, name) {
            override type QuoteTables = Tuple1[self.type]
        }
        $columns.addOne(c)
        c
    }

    def intColumn(name: String): TableColumnExpr[Int] { type QuoteTables = Tuple1[self.type] } = column[Int](name)

    def varcharColumn(name: String): TableColumnExpr[String] { type QuoteTables = Tuple1[self.type] } = column[String](name)

    def longColumn(name: String): TableColumnExpr[Long] { type QuoteTables = Tuple1[self.type] } = column[Long](name)

    def floatColumn(name: String): TableColumnExpr[Float] { type QuoteTables = Tuple1[self.type] } = column[Float](name)

    def doubleColumn(name: String): TableColumnExpr[Double] { type QuoteTables = Tuple1[self.type] } = column[Double](name)

    def booleanColumn(name: String): TableColumnExpr[Boolean] { type QuoteTables = Tuple1[self.type] } = column[Boolean](name)

    def dateColumn(name: String): TableColumnExpr[Date] { type QuoteTables = Tuple1[self.type] } = column[Date](name)

    def decimalColumn(name: String): TableColumnExpr[BigDecimal] { type QuoteTables = Tuple1[self.type] } = column[BigDecimal](name)
}

object TableSchema {
    inline given tableToQuery[T <: TableSchema]: Conversion[T, Query[T]] = Query[T](_)
}

type NothingTable <: TableSchema

extension[T <: TableSchema] (t: T) {
    inline infix def as(aliasName: String)(using NonEmpty[aliasName.type] =:= Any): AliasedTableSchema = {
        val columns = columnsMacro[T](t)
        AliasedTableSchema(t.tableName, aliasName, columns)
    }

    inline infix def unsafeAs(aliasName: String): AliasedTableSchema = {
        val columns = columnsMacro[T](t)
        AliasedTableSchema(t.tableName, aliasName, columns)
    }

    infix def join(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.FULL_JOIN, table)
}

case class AliasedTableSchema(tableName: String, aliasName: String, columns: Map[String, TableColumnExpr[?]]) extends Dynamic {
    def selectDynamic(name: String): TableColumnExpr[_] = columns(name).copy(table = aliasName)

    infix def join(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)
}

case class JoinTableSchema(left: TableSchema | JoinTableSchema | AliasedTableSchema, joinType: SqlJoinType, right: TableSchema | JoinTableSchema | AliasedTableSchema, var onCondition: Option[Expr[_]] = None) {
    infix def join(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasedTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)

    infix def on(expr: Expr[_]): JoinTableSchema = {
        this.onCondition = Some(expr)
        this
    }
}