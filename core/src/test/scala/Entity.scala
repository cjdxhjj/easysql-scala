import org.easysql.database.TableEntity
import org.easysql.dsl._

import java.util.Date

case class User(id: Int, key: String, name: Option[String]) extends TableEntity[(Int, String)]

class UserTable(aliasName: Option[String] = None) extends TableSchema[User](aliasName) {
    override val tableName: String = "user"
    val id = intColumn("id").incr.bind(_.id)
    val key = varcharColumn("test_key").primaryKey.bind(_.key)
    val name = varcharColumn("user_name").nullable.bind(_.name)
    val * = (id, key, name)
}

given user: UserTable = UserTable()

case class Post(id: Int, userId: Int, name: String) extends TableEntity[Int]

class PostTable(aliasName: Option[String] = None) extends TableSchema[Post](aliasName) {
    override val tableName: String = "post"
    val id = intColumn("id").incr
    val userId = intColumn("user_id")
    val name = varcharColumn("post_name")
    val * = (id, userId, name)
}

given post: PostTable = PostTable()