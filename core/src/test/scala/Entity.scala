import org.easysql.dsl
import org.easysql.dsl.*

import java.util.Date

@Table("user")
case class User(@IncrKey id: Int, @PrimaryKey key: String, @Column name: Option[String])

val user = asTable[User]

@Table("post")
case class Post(@IncrKey id: Int, @Column("user_id") userId: Int, @Column name: String)

val post = asTable[Post]

class NothingTable extends TableSchema() {
    override val tableName: String = "n"
    val id = intColumn("id").incr
    val name = varcharColumn("name")
    val * = (id, name)
}