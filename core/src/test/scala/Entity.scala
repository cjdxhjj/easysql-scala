import org.easysql.dsl
import org.easysql.dsl.*

import java.util.Date

case class User(id: Int, key: String, name: Option[String])

class UserTable extends TableSchema[User]() {
    override val tableName: String = "user"
    val id = intColumn("id").incr
    val key = varcharColumn("test_key").primaryKey
    val name = varcharColumn("user_name").nullable
    val * = (id, key, name)
}

given user: UserTable = UserTable()

case class Post(id: Int, userId: Int, name: String)

class PostTable extends TableSchema[Post] {
    override val tableName: String = "post"
    val id = intColumn("id").incr
    val userId = intColumn("user_id")
    val name = varcharColumn("post_name")
    val * = (id, userId, name)
}

given post: PostTable = PostTable()