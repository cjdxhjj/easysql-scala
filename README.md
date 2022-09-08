# 概述

easysql是一个使用Scala3编写的sql构造器，其充分利用了Scala3优秀的类型系统，可以在编译期解决掉绝大多数的错误sql。并且，得益于Scala强大的表达能力，api与原生sql非常相似，极大降低了学习成本。

虽然定位并非orm，但我们仍然可以把它当做轻量级的orm使用，比如执行查询，并把结果映射到类，或是使用case class直接生成insert、update等语句，避免样板代码。（此部分使用内联函数和宏实现，而非运行期反射，接近0开销）。

我们可以这样来构造一个查询模板：

```scala
val userName: Option[String] = ???

val s = (select (user.*, post.*)
        from user
        leftJoin Post on user.id === post.userId
        orderBy user.id.asc
        limit 10 offset 10)

s.where(userName.nonEmpty, user.name === userName)
```

库内置了一个sql的抽象语法树，sql的任何部分都可以被转化为对象或者方法调用，可以灵活地动态构造查询，这在某些应用（比如低代码平台）中会非常有价值：

```scala
val s = select(user.*)

// 根据不同条件查询不同表
if (true) {
    s.from(user)
    // 把条件封装在变量
    val condition = user.id === 1
    // 动态添加查询条件
    s.where(condition)
    // 动态拼装order by
    s.orderBy(user.id.asc)
} else {
    s.from(user).leftJoin(post).on(user.id === post.userId)
    // 动态添加查询列
    s.select(post.*)
}
```

相比使用字符串等方式，不必费尽心思的处理字符串拼接，并且，调用各种子句的顺序可以不囿于sql语句的顺序，也可以在生成sql时处理成正确的情况。

支持mysql、postgresql、oracle在内的多种方言。对同一个查询对象，可以方便地生成不同的数据库方言。

下面，我们开始了解如何使用easysql构造查询吧。

# 元数据配置

在大多数应用中，我们需要预先对数据表进行建模，比如创建数据库实体类，easysql支持从实体类中进行元数据抽取。

如果你的业务里，并不能事先知道表结构信息，可以先跳过此部分，后续部分将会介绍如何使用动态的表名和字段名来构造查询。

下面有一个`case class`组织的实体类：

```scala
case class User(id: Int, name: String, testOption: Option[String])
```

我们可以在类上添加注解`Table`，在字段上添加注解`Column`，主键字段添加`PrimaryKey`或`IncrKey`（对应自增主键）：

```scala
@Table("user")
case class User(@IncrKey id: Int, @Column name: String, @Column("test_option") testOption: Option[String])
```

注解的参数为实际的数据库表名或字段名，如果名字与字段名相同，则可以省略。

然后使用`asTable`来从实体类中生成表的元数据：

```scala
@Table("user")
case class User(@IncrKey id: Int, @Column name: String, @Column("test_option") testOption: Option[String])

val user = asTable[User]
```

`asTable`会从实体类中抽取字段表达式，并生成一个名为`*`的方法，返回一个字段元组，实际产生的类型如下：

```scala
// 下面的类型都是自动生成的，不需要显式写出，此处仅用于说明
val id: PrimaryKeyExpr[Int] = user.id
val name: TableColumnExpr[String] = user.name
val testOption: TableColumnExpr[String] = user.testOption
val all: (Expr[Int], Expr[String], Expr[String])  = user.*
```

这样我们就能用这些元数据来构造查询了：

```scala
// 查询
val s = select (user.*) from user where user.id === 1
val f = find[User](1)

// 增删改
val userRow = User(1, "x", None)
val i = insert(userRow)
val u = update(userRow)
val sv = save(userRow)
val d = delete[User](1)
```

更详细的构造api会在后文查询构造部分介绍。

# 查询构造

下面，我们开始了解查询构造器的使用，但是，在介绍select、insert等sql语句构造之前，需要先对sql的表达式有一些了解。

## 表达式

先来介绍常用的sql表达式，比如字段，常量，逻辑运算，聚合函数等（为了避免此部分变得冗长，不常用的case when、窗口函数等将会在后续的部分进行说明）。

库内部使用了抽象语法树来构建表达式，所以各种表达式可以互相嵌套，并拥有共通的父类`Expr`，带来了强大的抽象能力。

### 字段

字段是最基本的表达式，上文中，我们创建的元数据里，便是创建了字段类型的表达式。

有了元数据，我们就可以写这样的查询：

```scala
val s = select (user.id) from user
```

并且`asTable`会自动创建一个名为`*`的方法，查询时会自动展开表的全部字段：

```scala
val s = select (user.*) from user
```

但是有些应用里，表是运行期动态创建的，我们恐怕并不能构建出元数据对象，这时候可以使用`col`方法来创建一个动态字段；

```scala
val s = select (col("t.id")) from table("t")
```

`col`可以携带类型参数，我们可以精确指定字段类型：

```scala
val idCol = col[Int]("t.id")

val s = select (idCol) from table("t") where idCol > 1
```

库内置了一个名为`*`的方法，用来产生一个字段通配符：

```scala
val s = select (*) from table("t")
```

### 表达式别名

可以使用`as`方法，来给查询的表达式起别名：

```scala
val s = select (user.id as "c1", user.name as "c2") from user
```

当然，`as`方法可以推广到后续介绍的任何sql表达式类型，后文便不再赘述。

值得一提的是：`as`方法使用了一种名为`refinement type`的手段达到类型安全，`as`的参数如果为**空字符串**，则不能通过编译：

```scala
// 编译错误
val s = select (user.id as "") from user
```

如果别名需要运行期动态确定，可以使用`unsafeAs`方法。

### 值

有一些需求中，可能需要把值作为查询结果集的一列，比如：

```sql
SELECT 1 AS "col1"
```

为了实现这个需求，我们需要`import org.easysql.dsl.given`：

```scala
import org.easysql.dsl.given

val s = select (1 as "col1", "a" as "col2")
```

或是使用`value`方法来创建一个值表达式：

```scala
val s = select (value(1) as "col1", value("a") as "col2")
```

### 逻辑运算

除开字段和常量之外，最常见的表达式就是逻辑运算构成的表达式。

支持的二元逻辑运算有：`===`、`<>`、`>`、`>=`、`<`、`<=`、`&&`、`||`、`^`，使用方法就如同语言内置的运算符一样：

```scala
val id = 1
val name = "x"
val s = select (user.*) from user where user.id > id && user.name === name 
```

二元运算的右侧，不仅可以是值，也可以是其他表达式：

```scala
val s = select (user.*, post.*) from user join post on user.id === post.userId
```

当然，如果你想在运算左侧使用值，只需要`import org.easysql.dsl.given`：

```scala
import org.easysql.dsl.given

val s = select (user.*) from user where 1 === user.id
```

**`===`与`<>`在与`null`或`None`比较时，生成的sql为`IS NULL`与`IS NOT NULL。`**

除开这些符号组成的逻辑运算，还支持`in`、`notIn`、`between`、`notBetween`，`like`与`notLike`。这些运算符更推荐使用`.`加上方法名的方式调用，而非中缀的方式：

```scala
val s = select (user.*) from user where user.id.between(1, 5) || user.name.in("x", "y")

val s1 = select (user.*) from user where user.name.like("x%")
```

当然，这些运算符的抽象能力也一样强大，我们甚至可以写出来这种合法但无意义的sql：

```scala
val s = select (user.*) from user where user.id.in(1, 2, user.id)

import org.easysql.dsl.given
val s1 = select (user.*) from user where 1.in(user.id, 1)
```

此外还支持一元逻辑运算`!`：

```scala
val s = select (user.*) from user where !(user.id === 1)
```

生成的sql为`NOT()`。

### 数学运算

除开上面的逻辑运算外，还支持`+`、`-`、`*`、`/`、`%`五个数学运算：

```scala
val s = select (user.id * 100) from user where user.id + 1 > 5
```

如果要在表达式的左侧使用数值，别忘记`import org.easysql.dsl.given`。

### 聚合函数

接下来就是在实际场景中比较常见的聚合函数了，内置了几个常用的标准聚合函数：`count`、`countDistinct`、`max`、`min`、`sum`、`avg`：

```scala
val s = select (count() as "col1", sum(user.id) as "col2") from user
```

聚合函数也可以成为其他表达式的一部分：

```scala
val s = select (count() + user.id * 100 as "col1") from user
```

窗口函数使用的`rowNumber`、`rank`、`denseRank`聚合函数，以及自定义聚合函数，将在后续部分说明。

## 查询语句

好了，介绍完常用的表达式以后，就可以开始了解查询语句的构建了。

内置了一系列中缀方法，来构建查询，比如：

```scala
val s = select (user.*) from user where user.name === "x" orderBy user.id.asc
```

然后我们可以使用`sql`方法，传入数据库方言的枚举，来生成sql语句：

```scala
val sql: String = s.sql(DB.MYSQL)
```

如果不需要使用多种数据库，每次生成sql都要传入一个数据库类型，十分不方便，这个时候，我们可以创建一个`given`，然后使用`toSql`方法来生成sql。

```scala
given DB = DB.MYSQL

val s1 = select (user.id) from user
val sql1 = s1.toSql

val s2 = select (user.*) from user where user.name === "x"
val sql2 = s2.toSql
```

下面会介绍常用的select查询功能（不常用的功能比如cte、values临时表等会在后续部分介绍）。

### select

使用select方法，传入若干表达式，来创建select子句：

```scala
val s = select(user.id)
// 后续的select方法调用，会把字段追加在后面
s.select(user.name)
```

字段元组和普通的表达式可以放在一起，会在生成sql时递归展开：

```scala
val s = select (user.*, count())
```

select列表中的字段类型会在union查询或是子查询等时机保证类型安全，但要求你的查询字段是可以在编译期确定的。如果查询字段是运行期动态传入的，需要使用`dynamicSelect`：

```scala
// 假设此处查询的列是用户传入参数
val list = List("x", "y", "z")

// 把字符串列表转换为字段列表，并传入dynamicSelect中
val s = dynamicSelect(list.map(col): _*)
```

### from

使用中缀方法`from`，并传入元数据中配置的对象名：

```scala
val s = select (user.*) from user
```

如果你的表名也是运行期动态确定的，可以使用`table`方法：

```scala
val s = select (col("c1")) from table("t1")
```

from方法也可以传入一个子查询，会在后续子查询部分进行说明。

表可以起别名，可以用别名表来处理自连接：

```scala
val t1 = user as "t1"
val t2 = user as "t2"

val s = select (t1.*, t2.*) from t1 join t2 on t1.id === t2.id
```

如果别名是运行期确定，要使用`unsafeAs`方法。

### where

使用中缀方法`where`配合表达式来生成查询条件：

```scala
val s = select (user.*) from user where user.id === 1
```

如果你的查询条件都是使用`AND`来连接，那么也可以选择使用多个`where`：

```scala
val s = select (user.*) from user

s.where(user.id.in(1, 2, 3))
s.where(user.name === "x")
```

某些需要视情况而动态拼接的查询条件，可以在`where`中传入一个Boolean值或者返回Boolean的函数：

```scala
val name: Option[String] = ???

val s = select (user.*) from user
s.where(name.nonEmpty, user.name === name)
```

### order by

表达式类型中，有`asc`、`desc`两个方法，用来配合`orderBy`创建排序规则：

```scala
val s = select (user.*) from user orderBy (user.id.asc, user.name.desc)
```

### group by与having

使用`groupBy`做数据分组，`having`做分组后的筛选：

```scala
val s = select (user.name, count()) from user groupBy user.name having count() > 1
```

除了普通的group by之外，还支持几个特殊的group by形式：

1. `rollup`：
    ```scala
    select (user.id, user.name, count()) from user groupBy rollup(user.id, user.name)
    ```
2. `cube`：
    ```scala
    select (user.id, user.name, count()) from user groupBy cube(user.id, user.name)
    ```
3. `groupingSets`（参数类型支持表达式、表达式元组以及使用EmptyTuple产生的空元组）:
    ```scala
    select (user.id, user.name, count()) from user groupBy groupingSets((user.id, user.name), user.name)
    ```

上面的各种group by形式可以组合调用。

### distinct

使用`distinct`做数据去重：

```scala
val s = select (user.name) from user
s.distinct
```

### limit

使用limit和offset两个方法，来限制结果集：

```scala
val s = select (user.*) from user limit 10 offset 100
```

当只调用其中一个方法的时候，limit的缺省值为1，offset的缺省值为0。

### join

内置了`join`、`leftJoin`、`rightJoin`、`innerJoin`、`fullJoin`、`crossJoin`方法，配合`on`方法来连接表：

```scala
val s = select (a.*, b.*) from a join b on a.x === b.y
```

可以像真正的sql一样，使用小括号来限制表连接的顺序：

```scala
val s = select (a.*, b.*, c.*) from a join (b leftJoin c on b.y === c.z) on a.x === b.y
```

### 子查询

1. from中的子查询：
    
    **使用as方法对子查询起别名，并需要对子查询的每个字段起别名。然后使用类似`Tuple`的下标操作来引用子查询的字段**

    ```scala
    val sub = select (user.id as "c1", user.name as "c2") from user as "q1"

    val s = select (sub._1, sub._2) from sub
    ```

    join中的子查询与from的类似，此处不再赘述。

    可以使用`fromLateral`、`joinLateral`等来调用`lateral`子查询。

2. 表达式中的子查询：

    只返回一列的子查询可以被用在表达式里，比如：

    ```scala
    val sub = select (max(user.id)) from user

    val s = select (user.*) from user where user.id < sub
    ```

    支持`some`、`any`、`exists`、`notExists`、`all`等子查询谓词：

    ```scala
    val sub = select (max(user.id)) from user

    val s = select (user.*) from user where exists(sub)
    ```

3.  标量子查询

    只返回一列的查询可以被放入select列表中，这被称作标量子查询，我们需要把子查询的类型指定为Expr：

    ```scala
    val sub: Expr[Int] = select (user.id) from user
    val s = select (sub, user.name) from user
    ```

### for update

使用`forUpdate`方法来给查询加锁：

```scala
val s = select (user.*) from user
s.forUpdate
```

在sqlserver中会在FROM子句后生成WITH (UPDLOCK)。

### 生成sql

上文的链式调用，只是构建出了sql语法树，并未真正生成sql语句，我们需要一个终止操作来生成sql：

```scala
given DB = DB.MYSQL

val s = select (user.*) from user orderBy user.id.asc limit 10

val sql: String = s.toSql
val countSql: String = s.toCountSql
val pageSql: String = s.toPageSql(10, 1)
```

`toSql`会一比一的生成sql语句。

为了避免性能浪费，`toCountSql`会拷贝语法树副本，把limit信息和order by信息去掉，并把select列表替换为COUNT(*)：

```sql
SELECT COUNT(*) AS `count`
FROM `user`
```

`toPageSql`，顾名思义，就是生成分页的查询，第一个参数为每页条数，第二个参数为页码，会依据这两个参数替换语法树副本的信息。

## union查询

支持`union`、`unionAll`、`except`、`exceptAll`、`intersect`、`intersectAll`来将两个查询拼接在一起：

```scala
val s1 = select (user.name) from user where user.id === 1
val s2 = select (user.name) from user where user.id === 2

val union = s1 union s2
```

用于拼接的查询，select中的字段数目与字段类型必须一致，否则无法通过编译。

## 原生sql

虽然构造器的功能已经十分完善，但可能有一些特殊的方言，比如postgresql中的distinct on，oracle中的connect by在构造器中没有对应的api，这时候也可以使用`sql""`构建原生sql：

```scala
val id = 1
val s = sql"select * from user where id = $id"
```

这看起来就像是原生的字符串模板一样，如果熟悉scala的字符串模板，想必不会对这种方式感到陌生。

字符串等类型的变量代入其中的时候可以自动生成单引号：

```scala
val name = "x"
val s = sql"select * from user where name = $name"

assert(s == "select * from user where name = 'x'")
```

也可以把List代入其中：

```scala
val idList = List(1, 2, 3)
val s = sql"select * from user where id in $idList"

assert(s == "select * from user where id in (1, 2, 3)")
```

就算是使用原生sql，我们也不用编写生成表达式的样板代码。

## 插入语句

使用`insertInto`方法来创建insert语句：

```scala
val i = insertInto(user)(user.*).values((1, "x"), (2, "y"))

val sql = i.toSql
```

values中的每个元组的类型必须与insertInto中传入的参数一致，否则会编译错误。

配置好实体类上的元数据注解后，我们可以使用实体对象来插入数据：

```scala
val user = User(1, Some("x"))
val i = insert(user)
val sql = i.toSql
```

**使用incr标记的主键会在生成sql时跳过。**

insert语句也可以使用子查询：

```scala
val i = insertInto(user)(user.*).select {
    select (user.*) from user
}
```

`select`方法中子查询的返回类型也需要与`insertInto`中的传参一致。

## 更新语句

库对非主键字段扩展了一个名为`to`的方法，这样我们就可以在更新语句中使用：

```scala
val u = update(user).set(user.name to "x").where(user.id === 1)
val sql = u.toSql
```

由于是对非主键类型的扩展，所以更新主键将不会通过编译。

to跟一般的运算一样，右侧不止可以是值，也可以是其他表达式，比如我们这样实现某字段自增的需求：

```scala
val u = update(a).set(a.x to a.x + 1)
```

与insert类似，做好绑定后就可以使用实体类，来按主键更新其他字段：

```scala
val user = User(1, Some("x"))
val u = update(user)
val sql = u.toSql
```

使用实体类更新时，可以传入一个`skipNull`参数（默认为`true`），如果此参数为`true`，那么值为`null`或者值为`None`的字段会被跳过：

```scala
// 省略元数据配置代码
case class Entity(a: Int, b: Option[Int], c: Option[Int]) extends TableEntity[Int]

val e = Entity(1, Some(2), None)

// UPDATE entity SET b = 2 WHERE a = 1
val u1 = update(e)

// UPDATE entity SET b = 2, c = null WHERE a = 1
val u2 = update(e, skipNull = false)
```

## 删除语句

```scala
val d = deleteFrom(user).where(user.id === 1)
val sql = d.toSql
```

做好绑定后，便可以通过主键生成删除sql：

```scala
val d = delete[User](1)
val sql = d.toSql
```

如果参数类型与实体类注解定义的主键类型不一致，则会产生编译错误，如果是联合主键的表，此处依次传入多个参数或者一个对应类型的元组即可。

## 插入或更新

使用实体类生成按主键插入或更新的sql：
```scala
val user = User(1, Some("x"))
val s = save(user)
val sql = s.toSql
```

**每一种数据库生成的sql均不同。**

## 其他查询功能

上文中介绍的都是，常用的查询构造功能，下面介绍一些标准sql中的，不常用的功能。

### case when

使用`caseWhen()`方法和中缀函数`thenIs`与`elseIs`来生成一个case表达式：

```scala
val c = caseWhen(user.gender === 1 thenIs "男", user.gender === 2 thenIs "女") elseIs "其他"

select (c as "gender") from user
```

这会产生下面的查询：

```sql
SELECT CASE 
		WHEN user.gender = 1 THEN '男'
		WHEN user.gender = 2 THEN '女'
		ELSE '其他'
	END AS gender
FROM user
```

case when表达式也可以传入聚合函数中：

```scala
val c = caseWhen(user.gender === 1 thenIs user.gender) elseIs null

val select = select (count(c) as "male_count") from user
```

这会产生下面的查询：

```sql
SELECT COUNT(CASE 
		WHEN user.gender = 1 THEN user.gender
		ELSE NULL
	END) AS male_count
FROM user
```

### 窗口函数

在聚合函数后面调用`.over`，来创建一个窗口函数，然后通过`partitionBy`和`orderBy`来构建一个窗口：

```scala
select (rank().over partitionBy user.id orderBy user.name.asc as "over") from user
```

这会产生如下的查询：

```sql
SELECT RANK() OVER (PARTITION BY user.id ORDER BY user.user_name ASC) AS over FROM user
```

`partitionBy()`接收若干个表达式类型

`orderBy()`接收若干个排序列，在表达式类型之后调用`.asc`或`.desc`来生成排序规则。

**使用时需要注意数据库是否支持（比如mysql8.0以下版本不支持窗口函数功能）。**

### 自定义函数

除开内置的聚合函数外，我们还可以使用`NormalFunctionExpr`来创建普通函数，使用`AggFunctionExpr`来创建聚合函数，来看看下面的例子：

创建一个普通函数`LEFT`：

```scala
def left(e: Expr[String], size: Int): NormalFunctionExpr[String] = {
    import org.easysql.dsl.given
    NormalFunctionExpr("LEFT", List(e, size))
}

val s = select (user.*) from user where left(user.name, 3) === "xxx"
```

创建一个mysql的聚合函数GROUP_CONCAT：

```scala
def groupConcat(e: Expr[_], separator: String = "", distinct: Boolean = false, orderBy: OrderBy[_]*): AggFunctionExpr[String] = {
    import org.easysql.dsl.given
    AggFunctionExpr("GROUP_CONCAT", List(e), distinct, Map("SEPARATOR" -> separator), orderBy.toList)
}

val s1 = select (groupConcat(user.name, ",", true, user.id.asc)) from user

val s2 = select (groupConcat(user.name)) from user
```

这可能需要使用者对抽象语法树有一定的了解，如果你没有这种知识储备，那么也可以使用上文介绍的原生sql。

### cast转换

使用`cast`方法生成一个cast表达式用于数据库类型转换。

第一个参数为待转换的表达式；

第二个参数为String，为想转换的数据类型。

比如：

```scala
val select = select (cast[String](user.id, "CHAR")) from user
```

这会产生下面的查询：

```sql
SELECT CAST(user.id AS CHAR) FROM user
```

**会影响查询效率，不推荐使用**。

### with查询

`WithSelect()`生成一个with查询（mysql和pgsql使用递归查询在调用链添加.recursive）

```scala
val q1 = select (user.name) from user where user.id === 1 as "q1"
val q2 = select (user.name) from user where user.id === 2 as "q2"
val w = WithSelect()
    .add(q1, List("n1"))
    .add(q2, List("n2"))
    .query {
        select (q1.n1, q2.n2) from table("q1") join table("q2")
    }
```

### values临时表

```scala
val v = ValuesSelect().addRow(1, "x").addRow(2, "y")
```

values临时表最大的用处是被放入union中，但是上面的用法并不方便，我们可以使用`Tuple`或者`List[Tuple]`来简化调用：

```scala
val union = select (user.id, user.name) from user union (1, "x") union List((2, "y"), (3, "z"))
```

# 与数据库交互

上面我们了解了查询构造器，并知道了如何利用它来生成sql语句，但是这还不够。

实际项目中，我们需要连接到数据库，并修改数据库中的数据或是拿到查询结果。

目前，easysql添加了jdbc子项目，基于jdbc做了数据库交互的实现（以后会逐步添加其他的数据库驱动实现， 比如异步数据库连接）

下面我们以`jdbc`子项目为例，来介绍easysql与数据库交互的方式：

为了更好地使用`jdbc`，我们需要一个数据库连接池，此处以`druid`为例，使用者可以自行替换成实现了`DataSource`接口的连接池。

我们首先创建一个连接池，并交由`JdbcConnection`类管理：

```scala
// 此处省略连接池配置
val druid = new DruidDataSource()

// 此处第一个参数为连接池，第二个参数为数据库类型的枚举，用户可自行根据项目需要替换
val db = new JdbcConnection(druid, DB.MYSQL)
```

拿到了`JdbcConnection`的实例之后，我们可以在此之上操作数据库：

## 执行sql

对于insert、update、delete等修改数据的sql，我们可以使用`run`方法执行，并返回Int类型的受影响行数：

```scala
val insert = insertInto(User)(user.id, user.name).values((1, "x"), (2, "y"))
val result: Int = db.run(insert)
```

如果insert操作时，数据库中有自增主键，我们可以使用`runAndReturnKey`方法，返回一个List[Long]类型的结果集：

```scala
val insert = insertInto(User)(user.id, user.name)((1, "x"), (2, "y"))
val result: List[Long] = db.runAndReturnKey(insert)
```

## 接收查询结果

对于select、values临时表等查询类sql，可以使用`JdbcConnection`类进行查询，并返回查询结果。

支持的单条结果映射类型有三种：

1. 映射到继承了TableEntity的实体类（**可空字段将会被映射到Option类型**）；
2. 映射到一个Tuple，Tuple的实际类型取决于select方法的参数（**此时不能使用select \*或者无类型参数的col，否则会出现运行时异常**）；
3. 映射到一个Map[String, Any]，map的key为字段名（或查询中的别名），value为查询结果的值。

### 查询结果集

使用`queryMap`、`queryTuple`、`query`来查询结果集，返回结果是一个`List`，如果没有查询到结果，返回一个0元素的List：

```scala
val select = select (user.id, user.name) from user

val result1: List[Map[String, Any]] = db.queryMap(select)
val result2: List[(Int, String)] = db.queryTuple(select)
val result3: List[User] = db.query[User](select)
```

### 查询单条结果

使用`findMap`、`findTuple`、`find`来查询单条结果，返回结果是一个`Option`，如果没有查询到结果，返回一个None：

```scala
val select = select (user.id, user.name) from user

val result1: Option[Map[String, Any]] = db.findMap(select)
val result2: Option[(Int, String)] = db.findTuple(select)
val result3: Option[User] = db.find[User](1)
```

### 分页查询

使用`queryPageOfMap`、`queryPageOfTuple`、`queryPage`来进行分页查询，返回结果是一个`Page`类型，其定义如下：

```scala
case class Page[T](totalPage: Int = 0, totalCount: Int = 0, data: List[T] = List())
```

分页查询参数中除了需要传入查询dsl之外，还需要依次传入一页的结果集条数，页数，和是否需要查询count；

其中最后一个参数，默认值为true，为true时会附带执行一个查询count的sql，如无必要，请传入false，以便提升效率：

```scala
val select = select (user.id, user.name) from user

val result1: Page[Map[String, Any]] = db.queryPageOfMap(select)(10, 1)
val result2: Page[(Int, String)] = db.queryPageOfTuple(select)(10, 1, true)
val result3: Page[User] = db.queryPage[User](select)(10, 1, false)
```

### 查询count

使用`fetchCount`方法来查询结果集大小，返回结果是`Int`类型。

**此处会对生成的sql语法树进行复制，并去除对于查询count无用的order by和limit信息，并把select列表替换成COUNT(*)，以提高查询效率**：

```scala
val select = select (user.id, user.name) from user orderBy user.id.asc limit 10 offset 10

val count: Int = db.fetchCount(select)
```

此处实际生成的sql为：

```sql
SELECT COUNT(*) AS count FROM user
```

## 数据库事务

使用`transaction`函数来产生一个事务，该函数是一个高阶函数。

高阶函数中如果出现了异常，将会进行回滚，如无异常，将会提交事务，高阶函数结算后回收数据库连接：

```scala
db.transaction { t =>
    t.run(...) // 高阶函数里可以执行一些查询
    
    throw Exception() // 出现异常后会回滚事务
}
```

我们也可以手动传入`java.sql.Connection`中定义的隔离级别，比如：

```scala
db.transaction(TRANSACTION_READ_UNCOMMITTED) { t =>
    t.run(...)
}
```

# 致谢

easysql的scala版本的诞生，需要感谢两个人：

jilen：https://github.com/jilen

氘発孜然：https://github.com/daofaziran1

在此对二位进行诚挚的感谢！
