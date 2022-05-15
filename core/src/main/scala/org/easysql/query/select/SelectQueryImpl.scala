package org.easysql.query.select

import org.easysql.ast.statement.select.SqlUnionType
import org.easysql.dsl.{Expr, Union, SubQueryExpr, TableColumnExpr}

import java.sql.{Connection, SQLException}
import java.util.Date

abstract class SelectQueryImpl[T <: Tuple]() extends SelectQuery[T] {
    override infix def union[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.UNION, select)
    }

    override infix def union(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.UNION, values)
    }

    override infix def union(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.UNION, values)
    }

    override infix def unionAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.UNION_ALL, select)
    }

    override infix def unionAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.UNION_ALL, values)
    }

    override infix def unionAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.UNION_ALL, values)
    }

    override infix def except[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.EXCEPT, select)
    }

    override infix def except(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.EXCEPT, values)
    }

    override infix def except(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.EXCEPT, values)
    }

    override infix def exceptAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.EXCEPT_ALL, select)
    }

    override infix def exceptAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.EXCEPT_ALL, values)
    }

    override infix def exceptAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.EXCEPT_ALL, values)
    }

    override infix def intersect[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.INTERSECT, select)
    }

    override infix def intersect(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.INTERSECT, values)
    }

    override infix def intersect(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.INTERSECT, values)
    }

    override infix def intersectAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]] = {
        new UnionSelect(this, SqlUnionType.INTERSECT_ALL, select)
    }

    override infix def intersectAll(tuple: T): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        values.addRow(tuple)
        new UnionSelect[T](this, SqlUnionType.INTERSECT_ALL, values)
    }

    override infix def intersectAll(list: List[T]): UnionSelect[T] = {
        val values = new ValuesSelect[T]
        list.foreach(it => values.addRow(it))
        new UnionSelect[T](this, SqlUnionType.INTERSECT_ALL, values)
    }
}
