package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

inline def aliasMacro[T <: TableSchema[_]]: T = ${ aliasMacroImpl[T] }