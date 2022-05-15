package org.easysql.macros

import org.easysql.ast.SqlSingleConstType
import org.easysql.database.TableEntity
import org.easysql.query.delete.Delete

inline def deleteMacro[T <: TableEntity[_]](delete: Delete, primaryKey: Any): Delete = ${ deleteMacroImpl[T]('delete, 'primaryKey) }
