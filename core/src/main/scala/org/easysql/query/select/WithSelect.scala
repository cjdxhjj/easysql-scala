package org.easysql.query.select

import org.easysql.ast.expr.SqlIdentExpr
import org.easysql.ast.statement.select.{SqlSelectQuery, SqlWithItem, SqlWithSelect}
import org.easysql.database.DB
import org.easysql.visitor.getExpr
import org.easysql.dsl.col
import org.easysql.util.toSqlString

import java.sql.Connection

class WithSelect extends SelectQuery[EmptyTuple, EmptyTuple] {
    private val sqlWithSelect = SqlWithSelect()

    def recursive: WithSelect = {
        this.sqlWithSelect.recursive = true
        this
    }

    def add(query: Select[_, _], columns: List[String]): WithSelect = {
        val withItem = SqlWithItem(
            getExpr(col(query.aliasName.get)),
            query.getSelect,
            columns.map(it => SqlIdentExpr(it))
        )

        this.sqlWithSelect.withList.addOne(withItem)
        this
    }

    def query(query: SelectQuery[_, _]): WithSelect = {
        sqlWithSelect.query = Some(query.getSelect)
        this
    }

    override def sql(db: DB): String = toSqlString(sqlWithSelect, db)

    override def toSql(using db: DB): String = toSqlString(sqlWithSelect, db)

    override def getSelect: SqlSelectQuery = this.sqlWithSelect
}

object WithSelect {
    def apply(): WithSelect = new WithSelect()
}