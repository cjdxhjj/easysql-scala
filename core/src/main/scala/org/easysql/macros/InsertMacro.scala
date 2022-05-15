package org.easysql.macros

import org.easysql.database.TableEntity
import org.easysql.query.insert.Insert

inline def insertMacro[T <: TableEntity[_]](insert: Insert[_, _], entities: Seq[T]): Insert[_, _] = ${ insertMacroImpl('insert, 'entities) }

inline def valuesMacro[T <: TableEntity[_]](entity: T): Map[String, Any] = ${ valuesMacroImpl('entity) }