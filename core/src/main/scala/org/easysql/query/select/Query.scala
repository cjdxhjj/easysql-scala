package org.easysql.query.select

import org.easysql.dsl.*
import org.easysql.dsl.TableSchema
import org.easysql.ast.{SqlDataType, SqlNumberType}
import org.easysql.macros.*
import org.easysql.database.*
import org.easysql.util.toSqlString
import org.easysql.visitor.visitExpr

import scala.deriving.*

class Query[T](val t: T, val s: Select[_, _]) {
    private var tableNum = 1

    def map[R <: Expr[_] | TableSchema[_] | Tuple](f: T => R): Query[R] = {
        s.clear
        val mapResult = f(t)
        def addSelectItem(r: Any): Unit = r match {
            case e: Expr[_] => s.select(e)
            case ts: TableSchema[_] => {
                val cols = ts._cols.map(e => col(s"${ts._aliasName.get}.${e.column}")).toArray
                s.dynamicSelect(cols: _*)
            }
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
        val cols = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(joinTable._tableName, n, joinTable))
        val s = this.s.dynamicSelect(cols: _*).join(joinTable)

        val jt = inline this.t match {
            case tup: Tuple => tup ++ Tuple1(joinTable)
            case _          => Tuple2(this.t, joinTable)
        }

        new Query(jt.asInstanceOf[Append[T, TableSchema[E]]], s)
    }

    inline def leftJoin[E <: Product](using
        m: Mirror.ProductOf[E]
    ): Query[Append[T, TableSchema[E]]] = {
        tableNum += 1
        val joinTable = asTable[E].unsafeAs(s"t$tableNum")
        val cols = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(joinTable._tableName, n, joinTable))
        val s = this.s.dynamicSelect(cols: _*).leftJoin(joinTable)

        val jt = inline this.t match {
            case tup: Tuple => tup ++ Tuple1(joinTable)
            case _          => Tuple2(this.t, joinTable)
        }

        new Query(jt.asInstanceOf[Append[T, TableSchema[E]]], s)
    }

    inline def rightJoin[E <: Product](using
        m: Mirror.ProductOf[E]
    ): Query[Append[T, TableSchema[E]]] = {
        tableNum += 1
        val joinTable = asTable[E].unsafeAs(s"t$tableNum")
        val cols = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(joinTable._tableName, n, joinTable))
        val s = this.s.dynamicSelect(cols: _*).rightJoin(joinTable)

        val jt = inline this.t match {
            case tup: Tuple => tup ++ Tuple1(joinTable)
            case _          => Tuple2(this.t, joinTable)
        }

        new Query(jt.asInstanceOf[Append[T, TableSchema[E]]], s)
    }

    inline def fullJoin[E <: Product](using
        m: Mirror.ProductOf[E]
    ): Query[Append[T, TableSchema[E]]] = {
        tableNum += 1
        val joinTable = asTable[E].unsafeAs(s"t$tableNum")
        val cols = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(joinTable._tableName, n, joinTable))
        val s = this.s.dynamicSelect(cols: _*).fullJoin(joinTable)

        val jt = inline this.t match {
            case tup: Tuple => tup ++ Tuple1(joinTable)
            case _          => Tuple2(this.t, joinTable)
        }

        new Query(jt.asInstanceOf[Append[T, TableSchema[E]]], s)
    }

    inline def crossJoin[E <: Product](using
        m: Mirror.ProductOf[E]
    ): Query[Append[T, TableSchema[E]]] = {
        tableNum += 1
        val joinTable = asTable[E].unsafeAs(s"t$tableNum")
        val cols = fieldNamesMacro[E].toArray.map(n => TableColumnExpr(joinTable._tableName, n, joinTable))
        val s = this.s.dynamicSelect(cols: _*).crossJoin(joinTable)

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

    def drop(n: Int): Query[T] = {
        s.offset(n)
        this
    }

    def take(n: Int): Query[T] = {
        s.limit(n)
        this
    }

    def count: Query[Long] = {
        s.clear
        s.select(org.easysql.dsl.count())
        new Query(0, s)
    }

    def max[N <: SqlNumberType](f: T => Expr[N]): Query[Expr[N]] = {
        s.clear
        val item = f(t)
        s.select(org.easysql.dsl.max(item))
        new Query(item, s)
    }

    def min[N <: SqlNumberType](f: T => Expr[N]): Query[Expr[N]] = {
        s.clear
        val item = f(t)
        s.select(org.easysql.dsl.min(item))
        new Query(item, s)
    }

    def avg[N <: SqlNumberType](f: T => Expr[N]): Query[Expr[Number]] = {
        s.clear
        val item = org.easysql.dsl.avg(f(t))
        s.select(item)
        new Query(item, s)
    }

    def sum[N <: SqlNumberType](f: T => Expr[N]): Query[Expr[Number]] = {
        s.clear
        val item = org.easysql.dsl.sum(f(t))
        s.select(item)
        new Query(item, s)
    }

    def exists: Query[Boolean] = {
        val s = select(org.easysql.dsl.exists(this.s))
        new Query(false, s)
    }

    def resultType: List[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = List()

    def sql(db: DB): String = s.sql(db)

    def toSql(using db: DB): String = s.toSql
}

object Query {
    inline def apply[T <: Product](using m: Mirror.ProductOf[T]): Query[TableSchema[T]] = {
        val table = asTable[T].as("t1")
        val cols = fieldNamesMacro[T].toArray.map(n => TableColumnExpr(table._tableName, n, table))
        val s = dynamicSelect(cols: _*).from(table)
        new Query[TableSchema[T]](table, s)
    }
}
