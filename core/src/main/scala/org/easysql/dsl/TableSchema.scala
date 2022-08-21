package org.easysql.dsl

import org.easysql.database.TableEntity
import org.easysql.dsl.{AllColumnExpr, TableColumnExpr}
import org.easysql.ast.SqlDataType
import org.easysql.ast.table.SqlJoinType
import org.easysql.macros.*
import org.easysql.query.select.{Query, SelectQuery}

import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.collection.mutable

sealed trait AnyTable {
    infix def join(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)
}

trait TableSchema[E <: TableEntity[_]](val aliasName: Option[String] = None) extends AnyTable {
    val tableName: String

    val $columns: ListBuffer[TableColumnExpr[_, E] | NullableColumnExpr[_, E]] = ListBuffer[TableColumnExpr[_, E] | NullableColumnExpr[_, E]]()

    val $pkCols: ListBuffer[PrimaryKeyColumnExpr[_, E]] = ListBuffer[PrimaryKeyColumnExpr[_, E]]()

    def column[T <: SqlDataType](name: String): TableColumnExpr[T, E] = {
        val c = TableColumnExpr[T, E](aliasName.getOrElse(tableName), name, this)
        c
    }

    def intColumn(name: String): TableColumnExpr[Int, E] = column[Int](name)

    def varcharColumn(name: String): TableColumnExpr[String, E] = column[String](name)

    def longColumn(name: String): TableColumnExpr[Long, E] = column[Long](name)

    def floatColumn(name: String): TableColumnExpr[Float, E] = column[Float](name)

    def doubleColumn(name: String): TableColumnExpr[Double, E] = column[Double](name)

    def booleanColumn(name: String): TableColumnExpr[Boolean, E] = column[Boolean](name)

    def dateColumn(name: String): TableColumnExpr[Date, E] = column[Date](name)

    def decimalColumn(name: String): TableColumnExpr[BigDecimal, E] = column[BigDecimal](name)
}

object TableSchema {
    inline given tableToQuery[T <: TableSchema[_]]: Conversion[T, Query[T]] = Query[T](_)
}

extension[T <: TableSchema[_]] (t: T) {
    inline infix def as(aliasName: String)(using NonEmpty[aliasName.type] =:= Any): T =
        aliasMacro[T](aliasName)

    infix def unsafeAs(aliasName: String): TableSchema[_] = new TableSchema(Some(aliasName)) {
        override val tableName: String = t.tableName
    }
}

case class JoinTableSchema(left: AnyTable, joinType: SqlJoinType, right: AnyTable, var onCondition: Option[Expr[_]] = None) extends AnyTable {
    infix def on(expr: Expr[_]): JoinTableSchema = {
        this.onCondition = Some(expr)
        this
    }
}