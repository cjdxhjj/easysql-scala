package org.easysql.codegen

import org.easysql.database.JdbcConnection
import org.easysql.database.DB
import org.easysql.jdbc.jdbcQuery

import java.io.{File, PrintWriter}
import scala.collection.mutable

extension (dbc: JdbcConnection) {
    def generateEntity(dbName: String, path: String): Unit = {
        val dir = File(path)
        if (!dir.exists()) {
            println(dir.mkdir())
        }

        var packageName: String = path.replaceFirst("src/\\w+/\\w+", "").nn.replaceAll("/", ".").nn
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(1, packageName.length - 1).nn
        }
        dbc.getDB match {
            case DB.MYSQL => generateEntityForMysql(dbc, dbName, path, packageName)
            case DB.PGSQL => generateEntityForPgsql(dbc, dbName, path, packageName)
        }
    }
}

def generateEntityForMysql(dbConnection: JdbcConnection, dbName: String, path: String, packageName: String): Unit = {
    val conn = dbConnection.getConnection
    val tables = jdbcQuery(conn, "SHOW TABLES").map(_.toMap)
    if (tables.isEmpty) {
        return
    }

    val tableName = tables.map { it =>
        val key = it.keys.toList.head
        it(key).toString
    }

    tableName.foreach { it =>
        val columnSql = s"SELECT COLUMN_NAME, DATA_TYPE, EXTRA, COLUMN_KEY, IS_NULLABLE FROM information_schema.`COLUMNS` WHERE TABLE_SCHEMA = '$dbName' AND TABLE_NAME = '$it' ORDER BY ORDINAL_POSITION ASC"
        val columns = jdbcQuery(conn, columnSql).map(_.toMap)
        val entityCode = mutable.StringBuilder()
        var haveDate = false

        def getDataType(originDataType: String): String = {
            originDataType match {
                case "VARCHAR" | "CHAR" | "JSON" | "TEXT" | "LONGTEXT" | "MEDIUMTEXT" | "TINYTEXT" => "String"
                case "INT" | "TINYINT" | "SMALLINT" | "MEDIUMINT" => "Int"
                case "INTEGER" | "BIGINT" | "ID" => "Long"
                case "BIT" => "Boolean"
                case "FLOAT" => "Float"
                case "DOUBLE" => "Double"
                case "DATE" | "TIME" | "DATETIME" | "TIMESTAMP" | "YEAR" => {
                    haveDate = true
                    "Date"
                }
                case "DECIMAL" | "NUMERIC" => "BigDecimal"
                case _ => "Any"
            }
        }

        entityCode.append(s"case class ${lineToHump(it)}(")
        for (i <- columns.indices) {
            val dataType = getDataType(columns(i)("DATA_TYPE").toString.toUpperCase().nn)
            val columnDataType = if (columns(i)("IS_NULLABLE").toString.toUpperCase() == "YES") {
                "Option[" + dataType + "]"
            } else {
                dataType
            }
            entityCode.append(s"${lineToHump(columns(i)("COLUMN_NAME").toString, false)}: $columnDataType")
            if (i < columns.size - 1) {
                entityCode.append(", ")
            }
        }
        entityCode.append(") \n\n")
        entityCode.append(s"object ${lineToHump(it)} extends TableSchema {\n")
        entityCode.append(s"    override val tableName: String = \"$it\"")
        val cols = columns.filter { col =>
            getDataType(col("DATA_TYPE").toString.toUpperCase().nn) match {
                case "String" | "Int" | "Long" | "Boolean" | "Float" | "Double" | "Date" | "BigDecimal" => true
                case _ => false
            }
        }
        cols.foreach { col =>
            entityCode.append(s"\n    val ${lineToHump(col("COLUMN_NAME").toString, false)} = ")
            getDataType(col("DATA_TYPE").toString.toUpperCase().nn) match {
                case "String" => entityCode.append("varchar")
                case "Int" => entityCode.append("int")
                case "Long" => entityCode.append("long")
                case "Boolean" => entityCode.append("boolean")
                case "Float" => entityCode.append("float")
                case "Double" => entityCode.append("double")
                case "Date" => entityCode.append("date")
                case "BigDecimal" => entityCode.append("decimal")
            }
            entityCode.append(s"Column(\"${col("COLUMN_NAME").toString}\")")

            if (col("COLUMN_KEY").toString.toUpperCase() == "PRI") {
                if (col("EXTRA").toString.toUpperCase().nn.contains("AUTO_INCREMENT")) {
                    entityCode.append(".incr")
                } else {
                    entityCode.append(".primaryKey")
                }
            } else {
                if (col("IS_NULLABLE").toString.toUpperCase() == "YES") {
                    entityCode.append(".nullable")
                }
            }
        }

        val allColumn = cols.map(col => lineToHump(col("COLUMN_NAME").toString, false))
        if (allColumn.nonEmpty) {
            entityCode.append(s"\n    def * = (${allColumn.reduce((i, acc) => i + ", " + acc)})")
        }
        entityCode.append("\n}")

        val entity = mutable.StringBuilder()
        entity.append(s"package $packageName")
        entity.append("\n\n")
        entity.append("import org.easysql.dsl._")
        if (haveDate) {
            entity.append("\n\nimport java.util.Date")
        }
        entity.append("\n\n")
        entity.append(entityCode)

        val file = new File(path + lineToHump(it) + ".scala")
        val writer = PrintWriter(file)
        writer.write(entity.toString())
        writer.close()
    }

    conn.close()
}

def generateEntityForPgsql(dbConnection: JdbcConnection, dbName: String, path: String, packageName: String): Unit = {
    val conn = dbConnection.getConnection
    val tables = jdbcQuery(conn, s"SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tableowner = '$dbName'").map(_.toMap)
    if (tables.isEmpty) {
        return
    }

    val tableName = tables.map { it => it("tablename").toString }

    tableName.foreach { it =>
        val columnSql =
            s"SELECT column_name, udt_name, column_default, is_nullable FROM information_schema.\"columns\" WHERE table_catalog = '$dbName' AND table_name = '$it' ORDER BY ordinal_position ASC"
        val columns = jdbcQuery(conn, columnSql).map(_.toMap)

        val pkColSql = s"""
            SELECT
                pg_attribute.attname AS pkname
            FROM
                pg_constraint
            INNER JOIN pg_class ON
                pg_constraint.conrelid = pg_class.oid
            INNER JOIN pg_attribute ON
                pg_attribute.attrelid = pg_class.oid
                AND pg_attribute.attnum = pg_constraint.conkey[1]
            WHERE
                pg_class.relname = '$it'
                AND pg_constraint.contype = 'p'
        """
        val pkCols = jdbcQuery(conn, pkColSql).map(_.toMap).map(pk => pk("pkname").toString)

        val entityCode = mutable.StringBuilder()
        var haveDate = false

        def getDataType(originDataType: String): String = {
            originDataType match {
                case "VARCHAR" | "CHAR" | "JSON" | "JSONB" | "TEXT" => "String"
                case "INT2" | "INT4" => "Int"
                case "INT8" | "BIGINT" | "ID" => "Long"
                case "BIT" | "BOOL" => "Boolean"
                case "FLOAT4" => "Float"
                case "FLOAT8" | "MONEY" => "Double"
                case "TIME" | "TIMESTAMP" => {
                    haveDate = true
                    "Date"
                }
                case "NUMERIC" => "BigDecimal"
                case _ => "Any"
            }
        }

        entityCode.append(s"case class ${lineToHump(it)}(")
        for (i <- columns.indices) {
            val dataType = getDataType(columns(i)("udt_name").toString.toUpperCase().nn)
            val columnDataType = if (columns(i)("is_nullable").toString.toUpperCase() == "YES") {
                "Option[" + dataType + "]"
            } else {
                dataType
            }
            entityCode.append(s"${lineToHump(columns(i)("column_name").toString, false)}: $columnDataType")
            if (i < columns.size - 1) {
                entityCode.append(", ")
            }
        }
        entityCode.append(") \n\n")
        entityCode.append(s"object ${lineToHump(it)} extends TableSchema {\n")
        entityCode.append(s"    override val tableName: String = \"$it\"")
        val cols = columns.filter { col =>
            getDataType(col("udt_name").toString.toUpperCase().nn) match {
                case "String" | "Int" | "Long" | "Boolean" | "Float" | "Double" | "Date" | "BigDecimal" => true
                case _ => false
            }
        }
        cols.foreach { col =>
            entityCode.append(s"\n    val ${lineToHump(col("column_name").toString, false)} = ")
            getDataType(col("udt_name").toString.toUpperCase().nn) match {
                case "String" => entityCode.append("varchar")
                case "Int" => entityCode.append("int")
                case "Long" => entityCode.append("long")
                case "Boolean" => entityCode.append("boolean")
                case "Float" => entityCode.append("float")
                case "Double" => entityCode.append("double")
                case "Date" => entityCode.append("date")
                case "BigDecimal" => entityCode.append("decimal")
            }
            entityCode.append(s"Column(\"${col("column_name").toString}\")")

            if (pkCols.contains(col("column_name").toString)) {
                if (col("column_default").toString.toLowerCase().nn.contains("nextval")) {
                    entityCode.append(".incr")
                } else {
                    entityCode.append(".primaryKey")
                }
            } else {
                if (col("is_nullable").toString.toUpperCase() == "YES") {
                    entityCode.append(".nullable")
                }
            }
        }

        val allColumn = cols.map(col => lineToHump(col("column_name").toString, false))
        if (allColumn.nonEmpty) {
            entityCode.append(s"\n    def * = (${allColumn.reduce((i, acc) => i + ", " + acc)})")
        }
        entityCode.append("\n}")

        val entity = mutable.StringBuilder()
        entity.append(s"package $packageName")
        entity.append("\n\n")
        entity.append("import org.easysql.dsl._")
        if (haveDate) {
            entity.append("\n\nimport java.util.Date")
        }
        entity.append("\n\n")
        entity.append(entityCode)

        val file = new File(path + lineToHump(it) + ".scala")
        val writer = PrintWriter(file)
        writer.write(entity.toString())
        writer.close()
    }

    conn.close()
}

def humpToLine(string: String): String = {
    val builder = mutable.StringBuilder()
    for (i <- string.indices) {
        if (string(i).isUpper && i > 0) {
            builder.append("_")
        }
        builder.append(string(i).toLower)
    }
    builder.toString()
}

def lineToHump(string: String, firstUpper: Boolean = true): String = {
    val builder = mutable.StringBuilder()
    var upperCase = false
    for (i <- string.indices) {
        if (string(i) == '_') {
            upperCase = true
        } else {
            if (upperCase || i == 0 && firstUpper) {
                builder.append(string(i).toUpper)
                upperCase = false
            } else {
                builder.append(string(i))
            }
        }
    }
    builder.toString()
}


