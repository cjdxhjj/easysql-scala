package org.easysql.dsl

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

trait TableSchema[E <: Product] extends AnyTable with Dynamic with SelectItem[E] {
    val _tableName: String

    var _aliasName: Option[String] = None

    val _cols: ListBuffer[TableColumnExpr[_]] = ListBuffer()

    transparent inline def selectDynamic(inline name: String)(using m: Mirror.ProductOf[E]) = {
        inline exprMetaMacro[E](name) match {
            case ("pk", n) => PrimaryKeyColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type] & SqlDataType](_tableName, n, this)
            case ("incr", n) => PrimaryKeyColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type] & SqlDataType](_tableName, n, this, true)
            case (_, n) => TableColumnExpr[ElementType[m.MirroredElemTypes, m.MirroredElemLabels, name.type] & SqlDataType](_tableName, n, this)
        }
    }

    transparent inline def * (using m: Mirror.ProductOf[E]) = {
        val fields = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(_tableName, n, this))
        Tuple.fromArray(fields).asInstanceOf[ExprType[m.MirroredElemTypes]]
    }
}

extension [E <: Product, T <: TableSchema[E]] (t: T) {
    inline infix def as(aliasName: String)(using NonEmpty[aliasName.type] =:= Any): T = {
        val table = aliasMacro[E, T](t)
        table._aliasName = Some(aliasName)
        table
    }

    inline infix def unsafeAs(aliasName: String): T = {
        val table = aliasMacro[E, T](t)
        table._aliasName = Some(aliasName)
        table
    }
}

case class JoinTableSchema(
    left: AnyTable, 
    joinType: SqlJoinType, 
    right: AnyTable, 
    var onCondition: Option[Expr[_]] = None
) extends AnyTable {
    infix def on(expr: Expr[_]): JoinTableSchema = {
        this.onCondition = Some(expr)
        this
    }
}