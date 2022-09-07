package org.easysql.query.update

import org.easysql.ast.SqlDataType
import org.easysql.ast.expr.{SqlIdentifierExpr, SqlPropertyExpr}
import org.easysql.ast.statement.update.SqlUpdate
import org.easysql.database.DB
import org.easysql.dsl.*
import org.easysql.query.ReviseQuery
import org.easysql.query.select.SelectQuery
import org.easysql.util.toSqlString
import org.easysql.util.anyToExpr
import org.easysql.visitor.{getExpr, visitExpr}
import org.easysql.macros.*

import java.sql.{Connection, SQLException}
import java.util.Date

class Update extends ReviseQuery {
    private val sqlUpdate = SqlUpdate()

    def update(table: TableSchema[_]): Update = {
        this.sqlUpdate.table = Some(SqlIdentifierExpr(table.tableName))
        this
    }

    inline def update[T <: Product](entity: T, skipNull: Boolean = true): Update = {
        val (tableName, pkList, updateList) = updateMacro[T]

        sqlUpdate.table = Some(SqlIdentifierExpr(tableName))
        updateList.foreach { u =>
            val value = u._2.apply(entity)
            if (!skipNull || value != null) {
                val updatePair = getExpr(ColumnExpr(u._1)) -> getExpr(anyToExpr(value))
                sqlUpdate.setList.addOne(updatePair)
            }
        }

        if (sqlUpdate.setList.size == 0) {
            throw Exception("实体类中没有需要更新的字段")
        }

        pkList.foreach { pk =>
            sqlUpdate.addCondition(visitExpr(ColumnExpr(pk._1).equal(pk._2.apply(entity))))
        }

        this
    }

    def set[T <: SqlDataType | Null](items: (TableColumnExpr[_] | ColumnExpr[_], T | Expr[_] | SelectQuery[_])*): Update = {
        items.foreach { item =>
            val (column, value) = item

            val columnExpr = column match {
                case t: TableColumnExpr[_] => visitExpr(ColumnExpr(t.column))
                case c: ColumnExpr[_] => visitExpr(c)
            }

            val valueExpr = getExpr(value)
            sqlUpdate.setList.addOne(columnExpr -> valueExpr)
        }
        
        this
    }

    def where(condition: Expr[_]): Update = {
        sqlUpdate.addCondition(getExpr(condition))
        this
    }

    def where(test: () => Boolean, condition: Expr[_]): Update = {
        if (test()) {
            sqlUpdate.addCondition(getExpr(condition))
        }
        this
    }

    def where(test: Boolean, condition: Expr[_]): Update = {
        if (test) {
            sqlUpdate.addCondition(getExpr(condition))
        }
        this
    }

    override def sql(db: DB): String = toSqlString(sqlUpdate, db)

    override def toSql(using db: DB): String = toSqlString(sqlUpdate, db)
}

object Update {
    def apply(): Update = new Update()
}