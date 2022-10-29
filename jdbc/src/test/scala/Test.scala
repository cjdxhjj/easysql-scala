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
    val bind = bindQueryMacro[TestTable](0)
    val data = bind._2(Array[Any](1, null, Date()))
    println(data)

    // given Logger = java.util.logging.Logger.getLogger("")

    // val db: JdbcConnection = ???

    // val q = select(tt.id, tt.name) from tt

    // val data = db.query(q)
}

@Table("test_table")
case class TestTable(
    @IncrKey id: Int,
    @Column name: Option[String],
    @Column date: Option[Date]
)

val tt = asTable[TestTable]
