package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.dsl.MapUnionNull

trait DBTransaction(db: DB) extends DBOperater
