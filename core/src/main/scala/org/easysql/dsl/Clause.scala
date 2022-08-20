package org.easysql.dsl

import org.easysql.ast.SqlDataType
import org.easysql.ast.expr.SqlSubQueryPredicate
import org.easysql.database.{DB, TableEntity}
import org.easysql.dsl.ConstExpr
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.query.truncate.Truncate
import org.easysql.query.update.Update
import org.easysql.macros.findMacro

import scala.annotation.targetName
import scala.collection.mutable

def const[T <: SqlDataType | Null](value: T) = ConstExpr[T](value)

def col[T <: SqlDataType | Null](column: String) = ColumnExpr[T](column)

def caseWhen[T <: SqlDataType | Null](conditions: CaseBranch[T]*) = CaseExpr[T](conditions.toList)

def exists(select: SelectQuery[_]) = SubQueryPredicateExpr[Boolean](select, SqlSubQueryPredicate.EXISTS)

def notExists(select: SelectQuery[_]) = SubQueryPredicateExpr[Boolean](select, SqlSubQueryPredicate.NOT_EXISTS)

def all[T <: SqlDataType | Null](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.ALL)

def any[T <: SqlDataType | Null](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.ANY)

def some[T <: SqlDataType | Null](select: SelectQuery[Tuple1[T]]) = SubQueryPredicateExpr[T](select, SqlSubQueryPredicate.SOME)

def cast[T <: SqlDataType | Null](expr: Expr[_], castType: String) = CastExpr[T](expr, castType)

def table(name: String) = new TableSchema() {
    override val tableName: String = name
}

extension[T <: SqlDataType | Null] (e: TableColumnExpr[T] | ColumnExpr[T]) {
    def to[V <: T](value: V | Expr[V] | SelectQuery[Tuple1[V]]) = (e, value)
}

def * = AllColumnExpr()

def select[U <: Tuple](items: U): Select[RecursiveInverseMap[U]] = {
    val sel = Select().select(items)
    sel.asInstanceOf[Select[RecursiveInverseMap[U]]]
}

def select[I <: SqlDataType | Null](item: Expr[I]): Select[Tuple1[I]] = {
    val sel = Select().select(item)
    sel.asInstanceOf[Select[Tuple1[I]]]
}

def dynamicSelect(columns: Expr[_]*): Select[Tuple1[Nothing]] = Select().dynamicSelect(columns: _*)

inline def find[T <: TableEntity[_]](pk: PK[T]): Select[_] = findMacro[T](Select(), pk)

def insertInto(table: TableSchema)(columns: Tuple) = Insert().insertInto(table)(columns)

inline def insert[T <: TableEntity[_]](entity: T*) = Insert().insert(entity: _*)

inline def save[T <: TableEntity[_]](entity: T): Save = Save().save(entity)

def update(table: TableSchema): Update = Update().update(table)

inline def update[T <: TableEntity[_]](entity: T, skipNull: Boolean = true): Update = Update().update(entity, skipNull)

def deleteFrom(table: TableSchema): Delete = Delete().deleteFrom(table)

inline def delete[T <: TableEntity[_]](pk: PK[T]): Delete = Delete().delete[T](pk)

def truncate(table: TableSchema): Truncate = Truncate().truncate(table)

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