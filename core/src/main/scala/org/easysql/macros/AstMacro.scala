package org.easysql.macros

import org.easysql.ast.expr.*
import org.easysql.ast.statement.select.*

inline def astMacro(inline ast: SqlSelect): String = ${ astMacroImpl('ast) }
