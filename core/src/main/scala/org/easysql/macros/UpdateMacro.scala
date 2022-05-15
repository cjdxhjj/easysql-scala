package org.easysql.macros

import org.easysql.database.TableEntity
import org.easysql.query.update.Update

inline def updateMacro[T <: TableEntity[_]](update: Update, entity: T): Update = ${ updateMacroImpl[T]('update, 'entity) }
