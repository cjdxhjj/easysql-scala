package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

inline def columnsMacro[T <: TableSchema](table: T): Map[String, TableColumnExpr[_]] = ${ columnsMacroImpl[T]('table) }

inline def aliasMacro[T <: TableSchema](name: String): T = ${ aliasMacroImpl[T]('name) }