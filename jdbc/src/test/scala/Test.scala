import org.easysql.bind.bindEntityMacro
import org.easysql.dsl.*

import java.util.Date

object Test extends App {
    val bind = bindEntityMacro[TestTable]
    val data = bind(Map("id" -> 999, "name" -> "xxx", "test_option" -> "yyy", "date" -> Date("Sat Sep 03 01:26:06 CST 2019")))
    println(data)
}

@Table("test_table")
case class TestTable(
    @IncrKey id: Int,
    @Column name: String,
    @Column("test_nullable") testOption: String | Null,
    @Column date: Date
)

given tt: TableSchema[TestTable] = asTable[TestTable]

// inline def testBind[T <: Product](using t: TableSchema[T], ct: ClassTag[T]): T = {
//     val result = Map("id" -> 15, "user_id" -> 59, "post_name" -> "xxx")
//     bindData[T](result)
// }

// case class Post(id: Int, userId: Int, name: String) extends TableEntity[Int]

// class PostTable extends TableSchema[Post] {
//     override val tableName: String = "post"
//     val id = intColumn("id").primaryKey.bind(_.id)
//     val userId = intColumn("user_id").bind(_.userId)
//     val name = varcharColumn("post_name").bind(_.name)
// }

// given post: PostTable = new PostTable()