import org.easysql.dsl.*
import org.easysql.query.delete.Delete
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Query, Select, UnionSelect}
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



    val u1 = user as "u1"
    val u2 = user as "u2"
    val s = select(u1.*, u2.*) from u1 join u2 where u1.id === u2.id
    println(s.toSql)

    val s1 = select(user.id as "c1", user.name as "c2") from user as "s1"


    val c1 = s1._1
    val c2 = s1._2

    val s2 = select(c1, c2) from s1
    println(s2.toSql)

    val d = delete[User](1, "x")
    println(d.toSql)

    val userRow1 = User(1, "x", None)
    val userRow2 = User(2, "y", Some("x"))
    val i = insert(userRow1, userRow2)
    println(i.toSql)

    val update1 = update(userRow1, false)
    println(update1.toSql)
    val update2 = update(userRow2)
    println(update2.toSql)

    val save1 = save(userRow1)
    println(save1.sql(DB.MYSQL))
    println(save1.sql(DB.PGSQL))
    println(save1.sql(DB.SQLITE))
    println(save1.sql(DB.ORACLE))
}

case class Expr[T, E <: Entity](name: String, table: Schema[E], bind: Option[E => T] = None) {
    inline def bind(inline f: E => T): Expr[T, E] = {
        import scala.compiletime.codeOf
        val expr = Expr(name, table, Some(f))
        table.columns.append(expr)
        table.bind.put(codeOf(f).split("\\.").nn.apply(1).nn.split("\n").nn.head.nn, name)
        expr
    }
}

trait Entity

trait Schema[E <: Entity] {
    val bind: mutable.Map[String, String] = mutable.Map()

    val columns: ListBuffer[Expr[_, E]] = ListBuffer()

    def col[T](name: String): Expr[T, E] = {
        val expr = Expr[T, E](name, this)
        expr
    }
}

given a: Schema[ARow] = new A

case class ARow(id: Int, name: String) extends Entity

class A extends Schema[ARow] {
    val id = col[Int]("id").bind(_.id)
    val name = col[String]("name").bind(_.name)
}

def aaa[E <: Entity](e: E)(using t: Schema[E]): Unit = {
    t.columns.foreach { c =>
        println(c.name + "=" + c.bind.get.apply(e))
    }
}