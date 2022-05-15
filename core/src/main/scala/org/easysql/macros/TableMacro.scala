package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

inline def columnsMacro[T <: TableSchema](table: T): Map[String, TableColumnExpr[?]] = ${ columnsMacroImpl[T]('table) }
