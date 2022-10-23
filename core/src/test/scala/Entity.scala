import org.easysql.dsl
import org.easysql.dsl.*

import java.util.Date

@Table("user")
case class User(@IncrKey id: Int, @PrimaryKey key: String, @Column name: String)

val user = asTable[User]

@Table("post")
case class Post(@IncrKey id: Int, @Column("user_id") userId: Int, @Column name: String)

val post = asTable[Post]