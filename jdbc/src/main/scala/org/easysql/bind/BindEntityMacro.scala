package org.easysql.bind

inline def bindEntityMacro[T <: Product]: Map[String, Any] => T = ${ bindEntityMacroImpl[T] }