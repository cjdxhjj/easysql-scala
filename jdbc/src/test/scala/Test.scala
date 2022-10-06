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

    given Logger = java.util.logging.Logger.getLogger("")

    val db: JdbcConnection = ???

    val q = select(tt.id, tt.name) from tt

    val data = db.queryToList(q)
}

@Table("test_table")
case class TestTable(
    @IncrKey id: Int,
    @Column name: String,
    @Column date: Date
)

val tt = asTable[TestTable]
