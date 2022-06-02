package org.easysql.macros

import org.easysql.database.TableEntity
import org.easysql.query.update.Update

inline def updateMacro[T <: TableEntity[_]](update: Update, entity: T, skipNull: Boolean): Update = ${ updateMacroImpl[T]('update, 'entity, 'skipNull) }
