package org.easysql.query.select

import org.easysql.dsl.{ColumnExpr, Expr, NonEmpty}
import org.easysql.ast.SqlSingleConstType

import scala.language.dynamics

trait AliasNameQuery[T <: Tuple] extends SelectQuery[T] with Dynamic {
    var aliasName: Option[String] = None

    infix def as(name: String)(using NonEmpty[name.type] =:= Any): AliasNameQuery[T] = {
        this.aliasName = Some(name)
        this
    }

    infix def unsafeAs(name: String): AliasNameQuery[T] = {
        this.aliasName = Some(name)
        this
    }

    def selectDynamic(name: String): Expr[SqlSingleConstType | Null, EmptyTuple] = ColumnExpr[SqlSingleConstType | Null](s"${aliasName.get}.$name")
}