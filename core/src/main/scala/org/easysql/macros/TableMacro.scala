package org.easysql.macros

import org.easysql.dsl.{TableColumnExpr, TableSchema}

inline def aliasMacro[T <: TableSchema[_]]: T = ${ aliasMacroImpl[T] }

inline def fetchTableNameMacro[T <: Product]: String = ${ fetchTableNameMacroImpl[T] }

inline def exprMetaMacro[T](inline name: String): (String, String) = ${ exprMetaMacroImpl[T]('name) }

inline def fieldNamesMacro[T]: List[String] = ${ fieldNamesMacroImpl[T] }