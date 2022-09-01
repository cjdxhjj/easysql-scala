import org.easysql.dsl.*
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.select.*
import org.easysql.database.{DB, TableEntity}
import org.easysql.macros.*
import org.easysql.ast.SqlDataType
import org.easysql.ast.table.SqlJoinType

import java.lang.reflect.Constructor
import scala.compiletime.ops.any.*
import scala.compiletime.ops.int.+
import java.util.Date
import scala.annotation.{experimental, tailrec}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

object Test extends App {
    given DB = DB.MYSQL

//    val u1 = user as "u1"
//    val u2 = user as "u2"
//
//    val s = select(u1.*, u2.*) from u1 join u2 where u1.id === u2.id
//    println(s.toSql)
//
//    val s1 = select(user.id as "c1", user.name as "c2") from user as "s1"
//
//    val c1 = s1._1
//    val c2 = s1._2
//
//    val s2 = select(c1, c2) from s1
//    println(s2.toSql)
//
//    val d = delete[User](1, "x")
//    println(d.toSql)
//
//    val userRow1 = User(1, "x", None)
//    val userRow2 = User(2, "y", Some("x"))
//    val i = insert(userRow1, userRow2)
//    println(i.toSql)
//
//    val update1 = update(userRow1, false)
//    println(update1.toSql)
//    val update2 = update(userRow2)
//    println(update2.toSql)
//
//    val save1 = save(userRow1)
//    println(save1.sql(DB.MYSQL))
//    println(save1.sql(DB.PGSQL))
//    println(save1.sql(DB.SQLITE))
//    println(save1.sql(DB.ORACLE))
//
//    val f1 = find[User](1, "x")
//    println(f1.toSql)
//    val f2 = find[Post](1)
//    println(f2.toSql)

    val s = select (tt.*) from tt where tt.id === 1
    println(s.toSql)
}

@Table("test_table")
case class TestTable(@PrimaryKey id: Int,
                     @Column name: String,
                     @Column("test_option") testOption: Option[String])

given tt: TableSchema[TestTable] = asTable[TestTable]