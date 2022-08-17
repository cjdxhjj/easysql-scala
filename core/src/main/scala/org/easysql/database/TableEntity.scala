package org.easysql.database

import org.easysql.ast.SqlDataType

trait TableEntity[T <: SqlDataType | Tuple]