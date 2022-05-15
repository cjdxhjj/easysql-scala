package org.easysql.query.select

import org.easysql.ast.statement.select.SqlSelectQuery
import org.easysql.database.DB
import org.easysql.dsl.{Expr, Union, SubQueryExpr}
import org.easysql.query.BasedQuery

trait SelectQuery[T <: Tuple] extends BasedQuery {
    def getSelect: SqlSelectQuery

    infix def union[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def union(tuple: T): UnionSelect[T]
    
    infix def union(list: List[T]): UnionSelect[T]

    infix def unionAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def unionAll(tuple: T): UnionSelect[T]
    
    infix def unionAll(list: List[T]): UnionSelect[T]

    infix def except[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def except(tuple: T): UnionSelect[T]
    
    infix def except(list: List[T]): UnionSelect[T]

    infix def exceptAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def exceptAll(tuple: T): UnionSelect[T]
    
    infix def exceptAll(list: List[T]): UnionSelect[T]

    infix def intersect[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def intersect(tuple: T): UnionSelect[T]
    
    infix def intersect(list: List[T]): UnionSelect[T]

    infix def intersectAll[U <: Tuple](select: SelectQuery[U]): UnionSelect[Union[T, U]]
    
    infix def intersectAll(tuple: T): UnionSelect[T]
    
    infix def intersectAll(list: List[T]): UnionSelect[T]
}
