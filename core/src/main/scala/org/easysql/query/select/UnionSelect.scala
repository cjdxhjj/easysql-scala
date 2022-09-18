package org.easysql.query.select

import org.easysql.ast.statement.select.{SqlSelectQuery, SqlUnionSelect, SqlUnionType}
import org.easysql.database.DB
import org.easysql.util.toSqlString

import java.sql.Connection

class UnionSelect[T <: Tuple, AliasNames <: Tuple](val left: SelectQuery[_, _],
                  val operator: SqlUnionType,
                  val right: SelectQuery[_, _]) extends SelectQuery[T, AliasNames] {
    private val unionSelect = SqlUnionSelect(left.getSelect, operator, right.getSelect)

    override def sql(db: DB): String = toSqlString(unionSelect, db)

    override def toSql(using db: DB): String = toSqlString(unionSelect, db)

    override def getSelect: SqlSelectQuery = this.unionSelect
}
