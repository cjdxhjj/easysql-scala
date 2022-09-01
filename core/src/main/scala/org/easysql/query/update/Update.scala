package org.easysql.query.update

import org.easysql.ast.SqlDataType
import org.easysql.ast.expr.{SqlIdentifierExpr, SqlPropertyExpr}
import org.easysql.ast.statement.update.SqlUpdate
import org.easysql.database.{DB, TableEntity}
import org.easysql.dsl.*
import org.easysql.query.ReviseQuery
import org.easysql.query.select.SelectQuery
import org.easysql.util.toSqlString
import org.easysql.util.anyToExpr
import org.easysql.visitor.{getExpr, visitExpr}

import java.sql.{Connection, SQLException}
import java.util.Date

class Update extends ReviseQuery {
    private val sqlUpdate = SqlUpdate()

    def update(table: TableSchema[_]): Update = {
        this.sqlUpdate.table = Some(SqlIdentifierExpr(table.tableName))
        this
    }

//    def update[T <: TableEntity[_]](entity: T, skipNull: Boolean = true)(using t: TableSchema[T]): Update = {
//        sqlUpdate.table = Some(SqlIdentifierExpr(t.tableName))
//
//        t.$columns.foreach { col =>
//            val (column, value) = col match {
//                case TableColumnExpr(_, name, _, bind) => ColumnExpr(name) -> bind.get.apply(entity)
//                case NullableColumnExpr(_, name, _, bind) => ColumnExpr(name) -> bind.get.apply(entity)
//            }
//
//            if (!skipNull) {
//                sqlUpdate.setList.addOne(getExpr(column) -> getExpr(anyToExpr(value)))
//            } else {
//                if (value != null && value != None) {
//                    sqlUpdate.setList.addOne(getExpr(column) -> getExpr(anyToExpr(value)))
//                }
//            }
//        }
//
//        t.$pkCols.foreach { pk =>
//            sqlUpdate.addCondition(getExpr(pk.equal(pk.bind.get.apply(entity))))
//        }
//
//        this
//    }

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