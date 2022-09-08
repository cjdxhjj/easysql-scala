package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.{Select, SelectQuery, Query}
import org.easysql.query.update.Update
import org.easysql.jdbc.*
import org.easysql.dsl.TableSchema
import org.easysql.bind.*
import org.easysql.ast.SqlDataType

import java.sql.Connection
import javax.sql.DataSource

class JdbcConnection(db: DB, dataSource: DataSource) extends DBConnection(db) {
    def getDB: DB = db
   
    override inline def run(query: ReviseQuery): Int = 
        exec(conn => jdbcExec(conn, query.sql(db)))

    override inline def runAndReturnKey(query: Insert[_, _]): List[Long] = 
        exec(conn => jdbcExecReturnKey(conn, query.sql(db)))

    override inline def queryMap(query: SelectQuery[_]): List[Map[String, Any]] = 
        exec(conn => jdbcQuery(conn, query.sql(db)))

    override inline def queryTuple[T <: Tuple](query: SelectQuery[T]): List[T] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).map(Tuple.fromArray(_).asInstanceOf[T]))

    override inline def query[T <: Product](query: SelectQuery[_]): List[T] = 
        exec(conn => jdbcQuery(conn, query.sql(db)).map(it => bindEntityMacro[T](it)))

    override inline def queryMap(sql: String): List[Map[String, Any]] = 
        exec(conn => jdbcQuery(conn, sql))

    override inline def findMap(query: Select[_]): Option[Map[String, Any]] = 
        exec(conn => jdbcQuery(conn, query.sql(db)).headOption)

    override inline def findTuple[T <: Tuple](query: Select[T]): Option[T] = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).headOption.map(Tuple.fromArray(_).asInstanceOf[T]))

    override inline def find[T <: Product](pk: SqlDataType | Tuple): Option[T] = 
        exec(conn => jdbcQuery(conn, org.easysql.dsl.find[T](pk).sql(db)).headOption.map(it => bindEntityMacro[T](it)))

    override inline def queryPageOfMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]] =
        page(query)(pageSize, pageNum, needCount)(jdbcQuery)(it => it)

    override inline def queryPageOfTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T] =
        page(query)(pageSize, pageNum, needCount)(jdbcQueryToArray)(Tuple.fromArray(_).asInstanceOf[T])

    override inline def queryPage[T <: Product](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T] =
        page(query)(pageSize, pageNum, needCount)(jdbcQuery)(it => bindEntityMacro[T](it))

    override inline def fetchCount(query: Select[_]): Int = 
        exec(conn => jdbcQueryCount(conn, query.countSql(db)))

    inline def queryToList[T](query: Query[T]) = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).map(i => bindQuery[T].apply(i)))

    inline def queryToList[T <: Tuple](query: Select[T]) = 
        exec(conn => jdbcQueryToArray(conn, query.sql(db)).map(i => bindSelect[T].apply(i)))

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

    private def page[T, R](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean)(handler: (Connection, String) => List[R])(bind: R => T): Page[T] = {
        val data = if (pageSize == 0) {
            List[T]()
        } else {
            exec(conn => handler(conn, query.pageSql(pageNum, pageNum)(db)).map(bind))
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

        new Page[T](totalPage, count, data)
    }
}
