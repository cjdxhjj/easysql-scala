package org.easysql.jdbc

import java.sql.{Connection, Statement, ResultSet}
import java.util.Date
import java.time.{LocalDateTime, ZoneId}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.unsafeNulls


def jdbcQuery(conn: Connection, sql: String): List[Map[String, Any]] = {
    var stmt: Statement = null
    var rs: ResultSet = null
    val result = ListBuffer[Map[String, Any]]()

    try {
        stmt = conn.createStatement()
        rs = stmt.executeQuery(sql)
        val metadata = rs.getMetaData

        while (rs.next()) {
            val rowMap = (1 to metadata.getColumnCount()).map { it =>
                var data = rs.getObject(it)
                data match {
                    case b: java.math.BigDecimal => data = BigDecimal(b)
                    case _ =>
                }
                metadata.getColumnLabel(it) -> data
            }.toMap
            result.addOne(rowMap)
        }
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        stmt.close()
        rs.close()
    }

    result.toList
}

def jdbcQueryToArray(conn: Connection, sql: String): List[Array[Any]] = {
    var stmt: Statement = null
    var rs: ResultSet = null
    val result = ListBuffer[Array[Any]]()

    try {
        stmt = conn.createStatement()
        rs = stmt.executeQuery(sql)
        val metadata = rs.getMetaData

        while (rs.next()) {
            val rowList = (1 to metadata.getColumnCount()).toArray.map { it =>
                var data = rs.getObject(it)
                data match {
                    case b: java.math.BigDecimal => data = BigDecimal(b)
                    case _ =>
                }
                data.asInstanceOf[Any]
            }
            result.addOne(rowList)
        }
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        stmt.close()
        rs.close()
    }

    result.toList
}

def jdbcQueryCount(conn: Connection, sql: String): Int = {
    var stmt: Statement = null
    var rs: ResultSet = null
    var result = 0

    try {
        stmt = conn.createStatement()
        rs = stmt.executeQuery(sql)
        result = rs.getFetchSize
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        stmt.close()
        rs.close()
    }

    result
}

def jdbcExec(conn: Connection, sql: String): Int = {
    var stmt: Statement = null
    var result = 0

    try {
        stmt = conn.createStatement()
        result = stmt.executeUpdate(sql)
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        stmt.close()
    }

    result
}

def jdbcExecReturnKey(conn: Connection, sql: String): List[Long] = {
    var stmt: Statement = null
    val result = ListBuffer[Long]()

    try {
        stmt = conn.createStatement()
        stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)
        val resultSet = stmt.getGeneratedKeys
        while (resultSet.next()) {
            result += resultSet.getLong(1)
        }
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        stmt.close()
    }

    result.toList
}