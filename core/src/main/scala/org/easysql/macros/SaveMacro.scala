package org.easysql.macros

import org.easysql.database.TableEntity
import org.easysql.query.save.Save

inline def saveMacro[T <: TableEntity[_]](save: Save, entity: T): Save = ${ saveMacroImpl[T]('save, 'entity) }
