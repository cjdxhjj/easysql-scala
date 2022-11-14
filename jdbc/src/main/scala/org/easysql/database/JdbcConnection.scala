package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.query.update.Update
import org.easysql.jdbc.*
import org.easysql.dsl.*
import org.easysql.ast.SqlDataType
import org.easysql.dsl.FlatType

import java.sql.Connection
import javax.sql.DataSource

class JdbcConnection(override val db: DB, dataSource: DataSource) extends DBConnection(db) {
    def getDB: DB = db
   
    private[database] override def runSql(sql: String): Int = exec(jdbcExec(_, sql))

    private[database] override def runSqlAndReturnKey(sql: String): List[Long] = exec(jdbcExecReturnKey(_, sql))

    private[database] override def querySql(sql: String): List[Array[Any]] = exec(jdbcQueryToArray(_, sql))

    private[database] override def querySqlToMap(sql: String): List[Map[String, Any]] = exec(jdbcQuery(_, sql))

    private[database] override def querySqlCount(sql: String): Long = exec(jdbcQueryToArray(_, sql).head.head.toString().toLong)

    def transactionIsolation[T](isolation: Int)(query: JdbcTransaction ?=> T): T = {
        val conn = getConnection
        conn.setAutoCommit(false)
        conn.setTransactionIsolation(isolation)
       
        try {
            given t: JdbcTransaction = new JdbcTransaction(db, conn)
            val result = query
            conn.commit()
            result
        } catch {
            case e: Exception => {
                conn.rollback()
                throw e
            }
        } finally {
            conn.setAutoCommit(true)
            conn.close()
        }
    }
   
    def transaction[T](query: JdbcTransaction ?=> T): T = {
        val conn = getConnection
        conn.setAutoCommit(false)
       
        try {
            given t: JdbcTransaction = new JdbcTransaction(db, conn)
            val result = query
            conn.commit()
            result
        } catch {
            case e: Exception => {
                conn.rollback()
                throw e
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
