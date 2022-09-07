package org.easysql.query.select

import org.easysql.dsl.*
import org.easysql.dsl.TableSchema
import org.easysql.ast.SqlDataType
import org.easysql.macros.*
import org.easysql.database.*
import org.easysql.util.toSqlString

import scala.deriving.*

class Query[T](val t: T, val s: Select[_]) {
    private var tableNum = 1

    def map[R <: Expr[_] | TableSchema[_] | Tuple](f: T => R): Query[R] = {
        s.clear
        val mapResult = f(t)
        def addSelectItem(r: Any): Unit = r match {
            case e @ TableColumnExpr(t, n, schema) =>
                s.select(e)
            case p @ PrimaryKeyColumnExpr(t, n, schema, _) =>
                s.select(p)
            case e: Expr[_] => s.select(e)
            case t: Tuple => {
                val selectArray = t.toArray
                selectArray.foreach(addSelectItem(_))
            }
            case _ =>
        }
        addSelectItem(mapResult)
        new Query(mapResult, s)
    }

    def flatMap[R](f: T => Query[R]): Query[R] = ???

    def filter(f: T => Expr[Boolean]): Query[T] = {
        s.where(f(t))
        this
    }

    def withFilter(f: T => Expr[Boolean]): Query[T] = filter(f)

    inline def join[E <: Product](using
        m: Mirror.ProductOf[E]
    ): Query[Append[T, TableSchema[E]]] = {
        tableNum += 1
        val joinTable = asTable[E].unsafeAs(s"t$tableNum")
        val cols = fieldNamesMacro[E].toArray.map(n =>
            TableColumnExpr(joinTable.tableName, n, joinTable).unsafeAs(
              s"t${tableNum}__$n"
            )
        )
        val s = this.s.dynamicSelect(cols: _*).join(joinTable)

        val jt = inline this.t match {
            case tup: Tuple => tup ++ Tuple1(joinTable)
            case _          => Tuple2(this.t, joinTable)
        }

        new Query(jt.asInstanceOf[Append[T, TableSchema[E]]], s)
    }

    def on(f: T => Expr[Boolean]): Query[T] = {
        s.on(f(t))
        this
    }

    def groupBy[R <: Expr[_] | Tuple](f: T => R): Query[(R, T)] = {
        val group = f(t)
        group match {
            case e: Expr[_] => s.groupBy(e)
            case t: Tuple => {
                val groupArray = t.toArray.map(_.asInstanceOf[Expr[_]])
                s.groupBy(groupArray: _*)
            }
        }

        new Query((group, t), s)
    }

    def sortBy[R <: OrderBy | Tuple](f: T => R): Query[T] = {
        val order = f(t)
        order match {
            case o: OrderBy => s.orderBy(o)
            case t: Tuple => {
                val orderArray = t.toArray.map(_.asInstanceOf[OrderBy])
                s.orderBy(orderArray: _*)
            }
        }

        this
    }

    // def drop
    // def take

    def count: Query[Int] = {
        s.clear
        s.select(org.easysql.dsl.count())
        new Query(0, s)
    }

    // def max
    // def min
    // def avg
    // def sum

    def exists: Query[Boolean] = {
        val s = select(org.easysql.dsl.exists(this.s))
        new Query(false, s)
    }

    def resultType: List[FlatType[FlatType[T, SqlDataType | Null, Expr], Product, TableSchema]] = List()

    def sql(db: DB): String = s.sql(db)

    def toSql(using db: DB): String = s.toSql
}

object Query {
    inline def apply[T <: Product](using m: Mirror.ProductOf[T]): Query[TableSchema[T]] = {
        val table = asTable[T].as("t1")
        val cols = fieldNamesMacro[T].toArray.map(n =>
            TableColumnExpr(table.tableName, n, table).unsafeAs(s"t1__$n")
        )
        val s = dynamicSelect(cols: _*).from(table)
        new Query[TableSchema[T]](table, s)
    }
}
