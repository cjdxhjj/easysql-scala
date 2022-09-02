package org.easysql.macros

import org.easysql.dsl.TableSchema

inline def insertMacro[T <: Product]: (String, List[(String, T => Any)]) = ${ insertMacroImpl[T] }