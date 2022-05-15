package org.easysql.query

import org.easysql.database.DB

import java.sql.Connection

trait BasedQuery {
    def sql(db: DB): String

    def toSql(using db: DB): String
}
