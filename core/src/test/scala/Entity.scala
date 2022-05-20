import org.easysql.database.TableEntity
import org.easysql.dsl._

import java.util.Date

case class User(id: Int, key: String, name: Option[String]) extends TableEntity[(Int, String)]

object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").primaryKey
    val key = varcharColumn("key").primaryKey
    val name = varcharColumn("user_name").nullable
    def * = (id, key, name)
}

case class Post(id: Int, userId: Int, name: String) extends TableEntity[Int]

object Post extends TableSchema {
    override val tableName: String = "post"
    val id = intColumn("id").incr
    val userId = intColumn("user_id")
    val name = varcharColumn("post_name")
    def * = (id, userId, name)
}