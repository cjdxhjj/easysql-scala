package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.save.Save
import org.easysql.query.select.*
import org.easysql.query.update.Update
import org.easysql.dsl.MapUnionNull

trait DBConnection(db: DB) extends DBOperater
