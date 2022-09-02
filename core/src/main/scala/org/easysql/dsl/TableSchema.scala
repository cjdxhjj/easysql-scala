package org.easysql.dsl

import org.easysql.database.TableEntity
import org.easysql.dsl.{AllColumnExpr, TableColumnExpr}
import org.easysql.ast.SqlDataType
import org.easysql.ast.table.SqlJoinType
import org.easysql.macros.*
import org.easysql.query.select.SelectQuery

import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.language.dynamics
import scala.deriving.*

sealed trait AnyTable {
    infix def join(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.JOIN, table)

    infix def leftJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.LEFT_JOIN, table)

    infix def rightJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.RIGHT_JOIN, table)

    infix def innerJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.INNER_JOIN, table)

    infix def crossJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.CROSS_JOIN, table)

    infix def fullJoin(table: AnyTable): JoinTableSchema = JoinTableSchema(this, SqlJoinType.FULL_JOIN, table)
}

trait TableSchema[E <: Product] extends AnyTable with Dynamic {
    val tableName: String

    var aliasName: Option[String] = None
    
    def column[T <: SqlDataType](name: String): TableColumnExpr[T] = {
        val c = TableColumnExpr[T](aliasName.getOrElse(tableName), name, this)
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

    transparent inline def selectDynamic(inline name: String)(using m: Mirror.ProductOf[E]) = {
        inline exprMetaMacro[E](name) match {
            case ("pk", n) => PrimaryKeyColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type] & SqlDataType](tableName, n, this)
            case ("incr", n) => PrimaryKeyColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type] & SqlDataType](tableName, n, this, true)
            case (_, n) => TableColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type]](tableName, n, this)
        }
    }

    transparent inline def * (using m: Mirror.ProductOf[E]) = {
        val fields = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(tableName, n, this))
        Tuple.fromArray(fields).asInstanceOf[ExprType[m.MirroredElemTypes]]
    }
}

extension[E <: Product, T <: TableSchema[E]] (t: T) {
    inline infix def as(aliasName: String)(using NonEmpty[aliasName.type] =:= Any): T = {
        val table = aliasMacro[E, T](t)
        table.aliasName = Some(aliasName)
        table
    }

    infix def unsafeAs(aliasName: String): TableSchema[_] = {
        val table = new TableSchema {
            override val tableName: String = t.tableName
        }
        table.aliasName = Some(aliasName)
        table
    }
}

case class JoinTableSchema(left: AnyTable, joinType: SqlJoinType, right: AnyTable, var onCondition: Option[Expr[_]] = None) extends AnyTable {
    infix def on(expr: Expr[_]): JoinTableSchema = {
        this.onCondition = Some(expr)
        this
    }
}