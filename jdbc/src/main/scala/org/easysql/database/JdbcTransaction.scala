package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.jdbc.*
import org.easysql.dsl.TableSchema
import org.easysql.bind.*
import org.easysql.ast.SqlDataType

import java.sql.Connection

class JdbcTransaction(db: DB, conn: Connection) extends DBTransaction(db) {
    override inline def run(query: ReviseQuery): Int = jdbcExec(conn, query.sql(db))

    override inline def runAndReturnKey(query: Insert[_, _]): List[Long] = jdbcExecReturnKey(conn, query.sql(db))

    override inline def queryMap(query: SelectQuery[_]): List[Map[String, Any]] = jdbcQuery(conn, query.sql(db))

    override inline def queryTuple[T <: Tuple](query: SelectQuery[T]): List[T] = jdbcQueryToArray(conn, query.sql(db)).map(Tuple.fromArray(_).asInstanceOf[T])

    override inline def query[T <: Product](query: SelectQuery[_]): List[T] = jdbcQuery(conn, query.sql(db)).map(it => bindEntityMacro[T](it))

    override inline def queryMap(sql: String): List[Map[String, Any]] = jdbcQuery(conn, sql)

    override inline def findMap(query: Select[_]): Option[Map[String, Any]] = jdbcQuery(conn, query.sql(db)).headOption

    override inline def findTuple[T <: Tuple](query: Select[T]): Option[T] = jdbcQueryToArray(conn, query.sql(db)).headOption.map(Tuple.fromArray(_).asInstanceOf[T])

    override inline def find[T <: Product](pk: SqlDataType | Tuple): Option[T] = jdbcQuery(conn, org.easysql.dsl.find[T](pk).sql(db)).headOption.map(it => bindEntityMacro[T](it))

    override inline def queryPageOfMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]] =
        page(query)(pageSize, pageNum, needCount)(jdbcQuery)(it => it)

    override inline def queryPageOfTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T] =
        page(query)(pageSize, pageNum, needCount)(jdbcQueryToArray)(Tuple.fromArray(_).asInstanceOf[T])

    override inline def queryPage[T <: Product](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T] =
        page(query)(pageSize, pageNum, needCount)(jdbcQuery)(it => bindEntityMacro[T](it))

    override inline def fetchCount(query: Select[_]): Int = jdbcQueryCount(conn, query.countSql(db))

    private def page[T, R](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean)(handler: (Connection, String) => List[R])(bind: R => T): Page[T] = {
        val data = if (pageSize == 0) {
            List[T]()
        } else {
            handler(conn, query.pageSql(pageNum, pageNum)(db)).map(bind)
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
