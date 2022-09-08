import org.easysql.bind.*
import org.easysql.dsl.*
import org.easysql.ast.SqlDataType
import org.easysql.query.select.Query
import org.easysql.dsl.TableSchema
import org.easysql.database.*

import java.util.Date
import scala.compiletime.{erasedValue, error}
import scala.util.Random

object Test extends App {
    // val bind = bindEntityMacro[TestTable]
    // val data = bind(
    //   Map(
    //     "id" -> 999,
    //     "name" -> "xxx",
    //     "test_nullable" -> "yyy",
    //     "date" -> Date("Sat Sep 03 01:26:06 CST 2019")
    //   )
    // )
    // println(data)

    val q = query[TestTable].map(t => t -> t)
    val db: JdbcConnection = ???
    val data = db.queryToList(q)
}

@Table("test_table")
case class TestTable(
    @IncrKey id: Int,
    @Column name: String,
    @Column("test_nullable") testOption: String | Null,
    @Column date: Date
)

val tt = asTable[TestTable]
