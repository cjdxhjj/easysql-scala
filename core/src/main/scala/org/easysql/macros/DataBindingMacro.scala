package org.easysql.macros

import org.easysql.database.TableEntity

inline def bindEntityMacro[A <: TableEntity[_]]: Map[String, Any | Null] => A = ${ bindEntityMacroImpl[A] }