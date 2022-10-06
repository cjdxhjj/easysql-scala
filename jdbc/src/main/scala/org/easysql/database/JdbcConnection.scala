package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.query.update.Update
import org.easysql.jdbc.*
import org.easysql.dsl.*
import org.easysql.bind.*
import org.easysql.ast.SqlDataType
import org.easysql.dsl.FlatType

import java.sql.Connection
import javax.sql.DataSource
import reflect.Selectable.reflectiveSelectable

class JdbcConnection(db: DB, dataSource: DataSource) extends DBConnection(db) {
    def getDB: DB = db
   
    override inline def run(query: ReviseQuery)(using logger: Logger): Int = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcExec(conn, sql)
    }

    override inline def runAndReturnKey(query: Insert[_, _])(using logger: Logger): List[Long] = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcExecReturnKey(conn, sql)
    }

    override inline def queryToList(sql: String)(using logger: Logger): List[Map[String, Any]] = exec { conn =>
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQuery(conn, sql)
    }

    override inline def queryToList[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): List[EliminateTuple1[T]] = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).map(i => bindSelect[EliminateTuple1[T]].apply(i))
    }

    override inline def queryToList[T](query: Query[T])(using logger: Logger): List[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))
    }

    override inline def find[T <: Tuple](query: SelectQuery[T, _])(using logger: Logger): Option[EliminateTuple1[T]] = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).headOption.map(i => bindSelect[EliminateTuple1[T]].apply(i))
    }

    override inline def find[T](query: Query[T])(using logger: Logger): Option[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryToArray(conn, sql).headOption.map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i))
    }

    override inline def page[T <: Tuple](query: Select[T, _])(pageSize: Int, pageNum: Int, needCount: Boolean)(using logger: Logger): Page[EliminateTuple1[T]] = {
        val data = if (pageSize == 0) {
            List[EliminateTuple1[T]]()
        } else {
            val sql = query.pageSql(pageSize, pageNum)(db)
            logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
            exec(conn => jdbcQueryToArray(conn, sql).map(i => bindSelect[EliminateTuple1[T]].apply(i)))
        }

        val count = if (needCount) {
            fetchCount(query)(using logger)
        } else {
            0l
        }

        val totalPage = if (count == 0 || pageSize == 0) {
            0
        } else {
            if (count % pageSize == 0) {
                count / pageSize
            } else {
                count / pageSize + 1
            }
        }

        new Page[EliminateTuple1[T]](totalPage, count, data)
    }

    override inline def fetchCount(query: Select[_, _])(using logger: Logger): Long = exec { conn =>
        val sql = query.sql(db)
        logger.info(s"execute sql: ${sql.replaceAll("\n", " ")}")
        jdbcQueryCount(conn, sql)
    }

    def transaction(isolation: Int)(query: JdbcTransaction => Unit): Unit = {
        val conn = getConnection
        conn.setAutoCommit(false)
        conn.setTransactionIsolation(isolation)
       
        try {
            query(new JdbcTransaction(db, conn))
            conn.commit()
        } catch {
            case e: Exception => {
                e.printStackTrace()
                conn.rollback()
            }
        } finally {
            conn.setAutoCommit(true)
            conn.close()
        }
    }
   
    def transaction(query: JdbcTransaction => Unit): Unit = {
        val conn = getConnection
        conn.setAutoCommit(false)
       
        try {
            query(new JdbcTransaction(db, conn))
            conn.commit()
        } catch {
            case e: Exception => {
                e.printStackTrace()
                conn.rollback()
            }
        } finally {
            conn.setAutoCommit(true)
            conn.close()
        }
    }

    def getConnection: Connection = dataSource.getConnection.nn

    private def exec[T](handler: Connection => T): T = {
        val conn = getConnection
        val result = handler(conn)
        conn.close()
        result
    }
}
