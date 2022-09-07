import org.easysql.dsl.*
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.select.*
import org.easysql.database.DB
import org.easysql.macros.*
import org.easysql.ast.SqlDataType
import org.easysql.ast.table.SqlJoinType

import scala.compiletime.ops.any.*
import scala.compiletime.ops.int.+
import java.util.Date
import scala.annotation.{experimental, tailrec}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import java.util.Random

object Test extends App {
    given DB = DB.MYSQL

    val s = select(tt.*) from tt where tt.id === 1 && tt.testNullable === "x"
    println(s.toSql)

    val testTable = TestTable(1, "x", null)

    val i = insert(testTable)
    println(i.toSql)

    val u = update(testTable, false)
    println(u.toSql)

    val d = delete[TestTable](1)
    println(d.toSql)

    val sv = save[TestTable](testTable)
    println(sv.sql(DB.MYSQL))
    println(sv.sql(DB.PGSQL))
    println(sv.sql(DB.SQLITE))

    val f = find[TestTable](1)
    println(f.toSql)
}

@Table("test_table")
case class TestTable(
    @IncrKey id: Int,
    @Column name: String,
    @Column("test_nullable") testNullable: String | Null
)

val tt = asTable[TestTable]