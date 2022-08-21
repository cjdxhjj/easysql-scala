import org.easysql.database.TableEntity
import org.easysql.dsl.{PrimaryKeyColumnExpr, TableColumnExpr, TableSchema}
import org.easysql.bind.bindData

import scala.reflect.ClassTag

object Test extends App {
    val post = testBind[Post]
    println(post)
}

inline def testBind[T <: TableEntity[_]](using t: TableSchema[T], ct: ClassTag[T]): T = {
    val result = Map("id" -> 15, "user_id" -> 59, "post_name" -> "xxx")
    bindData[T](result)
}

case class Post(id: Int, userId: Int, name: String) extends TableEntity[Int]

class PostTable(alias: Option[String] = None) extends TableSchema[Post](alias) {
    override val tableName: String = "post"
    val id = intColumn("id").primaryKey.bind(_.id)
    val userId = intColumn("user_id").bind(_.userId)
    val name = varcharColumn("post_name").bind(_.name)
}

given post: PostTable = new PostTable()