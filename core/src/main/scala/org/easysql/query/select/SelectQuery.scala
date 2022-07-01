package org.easysql.query.select

import org.easysql.ast.statement.select.{SqlSelectQuery, SqlUnionType}
import org.easysql.database.DB
import org.easysql.dsl.{Expr, SubQueryExpr, Union}
import org.easysql.query.BasedQuery

trait SelectQuery[T <: Tuple] extends BasedQuery {
    def getSelect: SqlSelectQuery

    infix def union[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.UNION, select)
    }

    infix def union(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.UNION, values)
    }

    infix def union(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.UNION, values)
    }

    infix def unionAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.UNION_ALL, select)
    }

    infix def unionAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.UNION_ALL, values)
    }

    infix def unionAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.UNION_ALL, values)
    }

    infix def except[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.EXCEPT, select)
    }

    infix def except(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.EXCEPT, values)
    }

    infix def except(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.EXCEPT, values)
    }

    infix def exceptAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.EXCEPT_ALL, select)
    }

    infix def exceptAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.EXCEPT_ALL, values)
    }

    infix def exceptAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.EXCEPT_ALL, values)
    }

    infix def intersect[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.INTERSECT, select)
    }

    infix def intersect(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.INTERSECT, values)
    }

    infix def intersect(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.INTERSECT, values)
    }

    infix def intersectAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.INTERSECT_ALL, select)
    }

    infix def intersectAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.INTERSECT_ALL, values)
    }

    infix def intersectAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.INTERSECT_ALL, values)
    }
}
