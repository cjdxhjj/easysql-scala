import org.easysql.database.TableEntity
import org.easysql.dsl.{PrimaryKeyColumnExpr, TableColumnExpr, TableSchema}

import java.util.Date

case class User(id: Int, key: String, name: Option[String]) extends TableEntity[(Int, String)]

object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").primaryKey
    val key: PrimaryKeyColumnExpr[String] = varcharColumn("key").primaryKey
    val name: TableColumnExpr[String | Null] = varcharColumn("user_name").nullable
    def * = (id, key, name)
}

case class Post(id: Int, userId: Int, name: String) extends TableEntity[Int]

object Post extends TableSchema {
    override val tableName: String = "post"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").primaryKey
    val userId: TableColumnExpr[Int] = intColumn("user_id")
    val name: TableColumnExpr[String] = varcharColumn("post_name")
    def * = (id, userId, name)
}