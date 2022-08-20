package org.easysql.dsl

import org.easysql.database.TableEntity
import org.easysql.dsl.{AllColumnExpr, TableColumnExpr}
import org.easysql.ast.SqlDataType
import org.easysql.ast.table.SqlJoinType
import org.easysql.macros.columnsMacro
import org.easysql.query.select.{SelectQuery, Query}

import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.language.dynamics

trait AnyTable

trait TableSchema extends AnyTable { self =>
    val tableName: String

    var $columns: ListBuffer[TableColumnExpr[_]] = ListBuffer[TableColumnExpr[_]]()

    def column[T <: SqlDataType](name: String): TableColumnExpr[T] = {
        val c = TableColumnExpr[T](tableName, name)
        $columns.addOne(c)
        c
    }

    def intColumn(name: String): TableColumnExpr[Int] = column[Int](name)

    def varcharColumn(name: String): TableColumnExpr[String] = column[String](name)

    def longColumn(name: String): TableColumnExpr[Long] = column[Long](name)

    def floatColumn(name: String): TableColumnExpr[Float] = column[Float](name)

    def doubleColumn(name: String): TableColumnExpr[Double] = column[Double](name)

    def booleanColumn(name: String): TableColumnExpr[Boolean] = column[Boolean](name)

    def dateColumn(name: String): TableColumnExpr[Date] = column[Date](name)

    def decimalColumn(name: String): TableColumnExpr[BigDecimal] = column[BigDecimal](name)
}

object TableSchema {
    inline given tableToQuery[T <: TableSchema]: Conversion[T, Query[T]] = Query[T](_)
}

extension[T <: TableSchema] (t: T) {
    inline infix def as(aliasName: String)(using NonEmpty[aliasName.type] =:= Any): AliasNameTableSchema = {
        val columns = columnsMacro[T](t)
        AliasNameTableSchema(t.tableName, aliasName, columns)
    }

    inline infix def unsafeAs(aliasName: String): AliasNameTableSchema = {
        val columns = columnsMacro[T](t)
        AliasNameTableSchema(t.tableName, aliasName, columns)
    }

    infix def join(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(t, SqlJoinType.FULL_JOIN, table)
}

case class AliasNameTableSchema(tableName: String, aliasName: String, columns: Map[String, TableColumnExpr[_]]) extends Dynamic {
    val t: TableSchema = table(aliasName)

    def selectDynamic(name: String): TableColumnExpr[SqlDataType | Null] = columns(name).copy(table = aliasName).asInstanceOf[TableColumnExpr[SqlDataType | Null]]

    infix def join(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)
}

case class JoinTableSchema(left: TableSchema | JoinTableSchema | AliasNameTableSchema, joinType: SqlJoinType, right: TableSchema | JoinTableSchema | AliasNameTableSchema, var onCondition: Option[Expr[_]] = None) {
    infix def join(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: TableSchema | JoinTableSchema | AliasNameTableSchema): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)

    infix def on(expr: Expr[_]): JoinTableSchema = {
        this.onCondition = Some(expr)
        this
    }
}