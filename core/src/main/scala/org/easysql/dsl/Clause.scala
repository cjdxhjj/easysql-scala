package org.easysql.dsl

import org.easysql.ast.SqlDataType
import org.easysql.ast.expr.SqlSubQueryPredicate
import org.easysql.database.DB
import org.easysql.dsl.ConstExpr
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.query.truncate.Truncate
import org.easysql.query.update.Update
import org.easysql.macros.*

import scala.annotation.targetName
import scala.collection.mutable
import scala.deriving.*

def const[T <: SqlDataType](v: T) = ConstExpr[T](v)

def value[T <: SqlDataType](v: T) = ConstExpr[T](v)

def col[T <: SqlDataType](column: String) = ColumnExpr[T](column)

def caseWhen[T <: SqlDataType](conditions: CaseBranch[T]*) = CaseExpr[T](conditions.toList)

def exists(select: SelectQuery[_]) = SubQueryPredicateExpr[Boolean](select, SqlSubQueryPredicate.EXISTS)

def notExists(select: SelectQuery[_]) = SubQueryPredicateExpr[Boolean](select, SqlSubQueryPredicate.NOT_EXISTS)

def all[T <: SqlDataType](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.ALL)

def any[T <: SqlDataType](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.ANY)

def some[T <: SqlDataType](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.SOME)

def cast[T <: SqlDataType](expr: Expr[_], castType: String) = CastExpr[T](expr, castType)

def table(name: String) = new TableSchema() {
    override val tableName: String = name
}

inline def asTable[T <: Product] = new TableSchema[T] {
    override val tableName: String = fetchTableNameMacro[T]

    override val _cols: mutable.ListBuffer[TableColumnExpr[?]] = {
        val cols = mutable.ListBuffer[TableColumnExpr[_]]()
        val colList = fieldNamesMacro[T].map(n => TableColumnExpr(tableName, n, this))
        cols.addAll(colList)
        cols
    }
}

extension [T <: SqlDataType] (e: TableColumnExpr[T] | ColumnExpr[T]) {
    def to[V <: T](value: V | Expr[V] | SelectQuery[Tuple1[V]]) = (e, value)
}

object AllColumn {
    def * = AllColumnExpr()
}

def select[U <: Tuple](items: U): Select[RecursiveInverseMap[U]] = {
    val sel = Select().select(items)
    sel.asInstanceOf[Select[RecursiveInverseMap[U]]]
}

def select[I <: SqlDataType](item: Expr[I]): Select[Tuple1[I]] = {
    val sel = Select().select(item)
    sel.asInstanceOf[Select[Tuple1[I]]]
}

def select[P <: Product](table: TableSchema[P]): Select[Tuple1[P]] = {
    val sel = Select().select(table)
    sel.asInstanceOf[Select[Tuple1[P]]]
}

def dynamicSelect(columns: Expr[_]*): Select[Tuple1[Nothing]] = Select().dynamicSelect(columns: _*)

inline def find[T <: Product](pk: SqlDataType | Tuple): Select[Tuple1[T]] = {
    val (tableName, cols) = pkMacro[T, pk.type]

    val select = Select()
    select.from(table(tableName))
    inline pk match {
        case t: Tuple => t.toArray.zip(cols).foreach { (p, c) =>
            select.where(ColumnExpr(c).equal(p))
        }
        case _ => select.where(ColumnExpr(cols.head).equal(pk))
    }

    select.asInstanceOf[Select[Tuple1[T]]]
}

def insertInto(table: TableSchema[_])(columns: Tuple) = Insert().insertInto(table)(columns)

inline def insert[T <: Product](entities: T*) = Insert().insert(entities: _*)

inline def save[T <: Product](entity: T): Save = Save().save(entity)

def update(table: TableSchema[_]): Update = Update().update(table)

inline def update[T <: Product](entity: T, skipNull: Boolean = true): Update = Update().update(entity, skipNull)

def deleteFrom(table: TableSchema[_]): Delete = Delete().deleteFrom(table)

inline def delete[T <: Product](pk: SqlDataType | Tuple): Delete = Delete().delete[T](pk)

def truncate(table: TableSchema[_]): Truncate = Truncate().truncate(table)

inline def query[T <: Product](using m: Mirror.ProductOf[T]) = Query[T]

extension (s: StringContext) {
    def sql(args: (SqlDataType | List[SqlDataType])*): String = {
        import org.easysql.util.*
        import org.easysql.visitor.*

        val pit = s.parts.iterator
        val builder = mutable.StringBuilder(pit.next())
        args.foreach { arg =>
            val visitor = getOutPutVisitor(DB.MYSQL)
            val expr = getExpr(anyToExpr(arg))
            visitor.visitSqlExpr(expr)
            builder.append(visitor.sql())
            builder.append(pit.next())
        }
        builder.toString
    }
}
