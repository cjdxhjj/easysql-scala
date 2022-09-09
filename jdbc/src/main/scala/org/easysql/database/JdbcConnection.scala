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

import java.sql.Connection
import javax.sql.DataSource
import org.easysql.dsl.FlatType

class JdbcConnection(db: DB, dataSource: DataSource) extends DBConnection(db) {
    def getDB: DB = db
   
    override inline def run(query: ReviseQuery): Int = 
        exec(conn => jdbcExec(conn, query.sql(db)))

    override inline def runAndReturnKey(query: Insert[_, _]): List[Long] = 
        exec(conn => jdbcExecReturnKey(conn, query.sql(db)))

    override inline def queryToList(sql: String): List[Map[String, Any]] = 
        exec(conn => jdbcQuery(conn, sql))

    override inline def queryToList[T <: Tuple](query: SelectQuery[T]): List[EliminateTuple1[T]] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).map(i => bindSelect[EliminateTuple1[T]].apply(i)))

    override inline def queryToList[T](query: Query[T]): List[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i)))

    override inline def find[T <: Tuple](query: SelectQuery[T]): Option[EliminateTuple1[T]] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).headOption.map(i => bindSelect[EliminateTuple1[T]].apply(i)))

    override inline def find[T](query: Query[T]): Option[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).headOption.map(i => bindSelect[FlatType[FlatType[T, SqlDataType, Expr], Product, TableSchema]].apply(i)))

    override inline def page[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean): Page[EliminateTuple1[T]] = {
        val data = if (pageSize == 0) {
            List[EliminateTuple1[T]]()
        } else {
            exec(conn => jdbcQueryToArray(conn, query.pageSql(pageSize, pageNum)(db)).map(i => bindSelect[EliminateTuple1[T]].apply(i)))
        }

        val count = if (needCount) {
            fetchCount(query)
        } else {
            0
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

    override inline def fetchCount(query: Select[?]): Int = exec(conn => jdbcQueryCount(conn, query.countSql(db)))

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
