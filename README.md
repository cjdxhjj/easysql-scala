# 概述

easysql是一个使用Scala3编写的sql构造器，其充分利用了Scala3优秀的类型系统，可以在编译期解决掉绝大多数的错误sql。并且，得益于Scala强大的表达能力，api与原生sql非常相似，极大降低了学习成本。

支持mysql、postgresql、oracle在内的多种方言。对同一个查询对象，可以方便地生成不同的数据库方言。

虽然定位并非orm，但我们仍然可以把它当做轻量级的orm使用，比如执行查询，并把结果映射到类，或是使用case class直接生成insert、update等语句，避免样板代码。

我们可以使用原生sql风格构造查询：

```scala
val s = (select (User.*, Post.*)
        from User
        leftJoin Post on User.id === Post.userId
        orderBy User.id.asc
        limit 10 offset 10)
```

或是使用集合函数风格：

```scala
val s = User
    .joinLeft(Post)
    .on((u, p) => u.id === p.userId)
    .sortBy((u, _) => u.id.asc)
    .map((u, p) => u.* -> p.*)
    .drop(10)
    .take(10)
```

亦或是使用monadic风格：

```scala
val s = for {
    u <- User
    p <- Post if u.id === p.userId
} yield (u.*, p.*)
```

库内置了一个sql的ast（抽象语法树），使得我们可以灵活地对sql进行建模，动态构造查询，这在某些应用（比如低代码平台）中会非常有价值：

```scala
val s = select (User.*)

// 根据不同条件查询不同表
if (true) {
    s from User
    // 把条件封装在变量
    val condition = User.id === 1
    // 动态添加查询条件
    s where condition
    // 动态拼装order by
    s orderBy User.id.asc
} else {
    s from User leftJoin Post on User.id === Post.userId
    // 动态添加查询列
    s select Post.*
}
```

相比使用字符串等方式，不必费尽心思的处理字符串拼接，并且，调用各种子句的顺序可以不囿于sql语句的顺序，也可以在生成sql时处理成正确的情况。

下面，我们开始吧。

# 元数据配置

首先，我们需要创建一个数据表的元数据：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id")
    val name = varcharColumn("name")
}
```

上面，我们编写了一个`object`，并继承了`TableSchema`，添加了表名，以及两个字段。

字段类型支持：

`intColumn` `longColumn` `varcharColumn` `floatColumn` 

`doubleColumn` `booleanColumn` `dateColumn` `decimalColumn`

然后就可以这样使用查询：

```scala
val s = select (User.id, User.name) from User
```

## 主键和可空字段

对于主键字段，可以添加`.primaryKey`调用：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").primaryKey
    val name = varcharColumn("name")
}
```

自增主键添加`.incr`调用：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").incr
    val name = varcharColumn("name")
}
```

**更新主键字段会产生编译错误，而不是运行期异常，是类型安全的体现。**

使用`.nullable`来创建可空字段：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").primaryKey
    val name = varcharColumn("name").nullable
}
```

**非空字段会在与空值比较时产生编译错误。**

## 字段列表

为了更方便使用，推荐在元数据配置中添加一个字段元组：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").primaryKey
    val name = varcharColumn("name").nullable

    val * = (id, name)
}
```

然后就可以这样调用：

```scala
val s = select (User.*) from User
```

## 表结构继承

实际开发中，可能有很多表有一些共有字段，比如状态，创建时间等，如果每次都显式写出这些共有字段，会显得有些不方便。

这个时候我们可以编写一个`trait`继承`TableSchema`：

```scala
trait BaseTable extends TableSchema {
   lazy val dataState = intColumn("data_state")
   lazy val createTime = dateColumn("create_time")
}
```

其他表继承这个trait即可（**注意基表的字段需要写成`lazy val`**）：

```scala
object User extends BaseTable {
    override val tableName: String = "user"
    val id = intColumn("id").incr
    val name = varcharColumn("name").nullable
    val * = (id, name, dataState, createTime)
}
```

这样，我们就可以达到继承共有字段的目的了。

## 实体类绑定

将上面的object改为实体类的*伴生对象*，并且使对象的字段名和实体类的字段名对应，就可以将元数据绑定到实体类：

```scala
case class User(id: Int. name: Option[String]) extends TableEntity[Int]

object User extends TableSchema {
    override val tableName: String = "user"
    val id = intColumn("id").primaryKey
    val name = varcharColumn("name").nullable

    val * = (id, name)
}
```

**可空字段对应到Scala的Option类型，实体类需要继承TableEntity，类型参数即是主键类型，如果是联合主键，其类型为一个元组，顺序需要与object的主键配置对应。**

这样就能使用实体类来生成insert、update等语句：

```scala
val user = User(1, Some("x"))
val i = insert(user)

val u = update(user)

val d = delete[User](1)
```

更详细的使用方式，将会在后续部分说明。

## 代码生成

如果你使用的是mysql或者postgresql数据库，可以使用`jdbc`子项目中的工具来自动生成这些样板代码:

```scala
// 此处省略连接池配置
val ds: DataSource = ???
// 创建一个jdbc连接，传入数据库类型和连接池
val db = new JdbcConnection(DB.MYSQL, ds)
// 传入数据库名称和生成路径，来生成代码
db.generateEntity("test", "src/main/scala/codegen/")
```

再根据实际需求微调即可。

# 查询构造

下面，我们开始了解查询构造器的使用，但是，在介绍select、insert等sql语句之前，需要先对sql的表达式有一些了解。

## 表达式

先来介绍常用的sql表达式，比如字段，常量，逻辑运算，聚合函数等（为了避免此部分变得冗长，不常用的case when、窗口函数等将会在后续的部分进行说明）。

库内部使用了抽象语法树来构建表达式，所以各种表达式可以互相嵌套，并拥有共通的父类`Expr`，带来了强大的抽象能力。

### 字段

字段是最基本的表达式，上文中，我们创建的元数据里，便是创建了字段类型的表达式。

有了元数据对象，我们就可以写这样的查询：

```scala
val s = select (User.id) from User
```

但是有些应用里，表是运行期动态创建的，我们恐怕并不能构建出元数据对象，这时候可以使用`col`方法来创建一个动态字段；

```scala
val s = select (col("t.id")) from table("t")
```

库内置了一个名为\*的方法，用来产生一个字段通配符：

```scala
val s = select (*) from table("t")
```

### 表达式别名

可以使用`as`方法，来给查询的表达式起别名：

```scala
val s = select (User.id as "c1", User.name as "c2") from User
```

当然，`as`方法可以推广到后续介绍的任何sql表达式类型，后续就不再赘述了。

`as`方法使用了一种名为`refinement type`的手段达到类型安全，`as`的参数如果为空字符串，则不能通过编译：

```scala
// 编译错误
val s = select (User.id as "") from User
```

如果别名需要运行期动态确定，可以使用`unsafeAs`方法。

### 常量

有一些需求中，可能需要把常量作为查询结果集的一列，比如：

```sql
SELECT 1 AS "col1"
```

为了实现这个需求，我们需要`import org.easysql.dsl.given`：

```scala
val s = select (User.id as "col1", 1 as "col2", "x" as "col3") from User
```

### 逻辑运算

除开字段和常量之外，最常见的表达式就是逻辑运算构成的表达式。

支持的二元逻辑运算有：`===`、`<>`、`>`、`>=`、`<`、`<=`、`&&`、`||`、`^`，我们可以使用逻辑运算构成表达式，来代入查询条件里：

```scala
val id = 1
val name = "x"
val s = select (User.*) from User.id > id && User.name === name 
```

二元运算的右侧，不仅可以是值，也可以是其他表达式：

```scala
val s = select (User.*, Post.*) from User join Post on User.id === Post.userId
```

当然，如果你想在运算左侧使用常量，也一样可做到，前提是`import org.easysql.dsl.given`：

```scala
import org.easysql.dsl.given

val s = select (User.*) from User where 1 === User.id
```

**`===`与`<>`在与空值比较时，生成的sql为`IS NULL`与`IS NOT NULL。`**

除开这些符号组成的逻辑运算，还支持`in`、`notIn`、`between`、`notBetween`，以及String类型的表达式**专用**的`like`与`notLike`。与上面的符号略微有区别的是，这些字母组成的运算符，使用的时候需要套一层小括号：

```scala
val s = select (User.*) from User where (User.id between (1, 5)) || (User.name in ("x", "y"))

val s1 = select (User.*) from User where (User.name like "x%")
```

当然，这些运算符的抽象能力也一样强大，我们甚至可以写出来这种合法但无意义的sql：

```scala
val s = select (User.*) from User where (User.id in (1, 2, User.id))

import org.easysql.dsl.given
val s1 = select (User.*) from User where (1 in (User.id, 1))
```

### 数学运算

除开上面的逻辑运算外，还支持`+`、`-`、`*`、`/`、`%`五个数学运算：

```scala
val s = select (User.id * 100) from User where User.id + 1 > 5
```

当然，如果要在表达式的左侧使用数值，别忘记`import org.easysql.dsl.given`。

### 聚合函数

接下来，在实际工作中比较常见的表达式就是聚合函数了，内置了几个常用的标准聚合函数：`count`、`countDistinct`、`max`、`min`、`sum`、`avg`：

```scala
val s = select (count() as "col1", sum(User.id) as "col2") from User
```

聚合函数也可以成为其他表达式的一部分：

```scala
val s = select (count() + User.id * 100 as "col1") from User
```

当然，还支持窗口函数使用的`rowNumber`、`rank`、`denseRank`聚合函数，以及自定义聚合函数，但是受篇幅限制，将在后续部分说明。

## 查询语句

好了，介绍完常用的表达式以后，就可以开始了解sql的核心，查询语句的构建了。

内置了一系列中缀方法，来构建查询，比如：

```scala
val s = select (User.*) from User where User.name === "x" orderBy User.id.asc
```

然后我们可以使用`sql`方法，传入数据库方言的枚举，来生成sql语句：

```scala
val sql: String = s.sql(DB.MYSQL)
```

如果不需要使用多种数据库，每次生成sql都要传入一个数据库类型，十分不方便，这个时候，我们可以创建一个`given`，然后使用`toSql`方法来生成sql。

```scala
given DB = DB.MYSQL

val s1 = select (User.id) from User
val sql1 = s1.toSql

val s2 = select (User.*) from User where User.name === "x"
val sql2 = s2.toSql
```

这时候，你可能觉得，使用这种dsl，没办法在编译期检查这种错误的sql：

```scala
val s = select (A.*) from B
```

但是，使用`asSql`方法的话，这种错误也逃不过编译器的检查：

```scala
given DB = DB.MYSQL

val s = select (A.*) from B
val sql = s.asSql // 编译错误
```

**`asSql`更适合编译期模板可以确定的偏静态的查询。**

下面，来介绍常用的select子句（不常用的功能比如cte、values临时表等会在后续部分介绍）。

### select

使用select方法，传入若干表达式，来创建select子句：

```scala
val s = select(User.id)
// 后续的select方法调用，会把字段追加在后面
s.select(User.name)
```

字段元组和普通的表达式可以放在一起，会在生成sql时递归展开：

```scala
val s = select (User.*, count())
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
val s = select (User.*) from User
```

如果你的表名也是运行期动态确定的，可以使用`table`方法：

```scala
val s = select (col("c1")) from table("t1")
```

from方法也可以传入一个子查询，会在后续子查询部分进行说明。

表可以起别名，比如用自连接来处理树状数据：

```scala
val t1 = User as "t1"
val t2 = User as "t2"

val s = select (t1.id, t2.name) from t1 join t2 
```

**暂时不支持别名表使用\*。**

### where

使用中缀方法`where`配合表达式来生成查询条件：

```scala
val s = select (User.*) from User where User.id === 1
```

如果你的查询条件都是使用`AND`来连接，那么也可以选择使用多个`where`：

```scala
val s = select (User.*) from User

s.where(User.id in (1, 2, 3))
s.where(User.name === "x")
```

某些需要视情况而动态拼接的查询条件，可以在`where`中传入一个Boolean值或者返回Boolean的函数：

```scala
val name = ""

val s = select (User.*) from User
s.where(name.nonEmpty, User.name === name)
```

### order by

表达式类型中，有`asc`、`desc`两个方法，用来配合`orderBy`创建排序规则：

```scala
val s = select (User.*) from User orderBy (User.id.asc, User.name.desc)
```

### group by与having

使用`groupBy`做数据分组，`having`做分组后的筛选：

```scala
val s = select (User.name, count()) from User groupBy User.name having count() > 1
```

除了普通的group by之外，还支持几个特殊的group by形式：

1. `rollup`：
    ```scala
    select (User.id, User.name, count()) from User groupBy rollup(User.id, User.name)
    ```
2. `cube`：
    ```scala
    select (User.id, User.name, count()) from User groupBy cube(User.id, User.name)
    ```
3. `groupingSets`（参数类型支持表达式、表达式元组以及使用EmptyTuple产生的空元组）:
    ```scala
    select (User.id, User.name, count()) from User groupBy groupingSets((User.id, User.name), User.name)
    ```

上面的各种group by形式可以组合调用。

### distinct

使用`distinct`做数据去重：

```scala
val s = select (User.name) from User
s.distinct
```

### limit

使用limit和offset两个方法，来限制结果集：

```scala
val s = select (User.*) from User limit 10 offset 100
```

当只调用其中一个方法的时候，limit的缺省值为1，offset的缺省值为0。

### join

内置了`join`、`leftJoin`、`rightJoin`、`innerJoin`、`fullJoin`、`crossJoin`方法，配合`on`方法来连接表：

```scala
val s = select (A.*, B.*) from A join B on A.x === B.y
```

可以像真正的sql一样，使用小括号来限制表连接的顺序：

```scala
val s = select (A.*, B.*, C.*) from A join (B join C on B.y === C.z) on A.x === B.y
```

### 子查询

1. from中的子查询：

    ```scala
    val subQuery = select (User.id as "col1") from User as "q1"

    val s = select (subQuery.col1) from subQuery
    ```

    **使用as方法对子查询起别名，并需要对子查询的每个字段起别名，字段别名可以当做子查询变量的属性来使用。**

    join中的子查询与from的类似，此处不再赘述。

    可以使用`fromLateral`、`joinLateral`等来调用lateral子查询。

2. 表达式中的子查询：

    只返回一列的子查询可以被用在表达式里，比如：

    ```scala
    val subQuery = select (max(User.id)) from User

    val s = select (User.*) from User where User.id < subQuery
    ```

    支持`some`、`any`、`exists`、`notExists`、`all`这几个子查询谓词：

    ```scala
    val subQuery = select (max(User.id)) from User

    val s = select (User.*) from User where exists(subQuery)
    ```

3.  标量子查询

    只返回一列的查询可以被放入select列表中，这被称作标量子查询，我们需要把子查询的类型指定为Expr：

    ```scala
    val sub: Expr[Int, EmptyTuple] = select (User.id) from User
    val s = select (sub, User.name) from User
    ```

### for update

使用`forUpdate`方法来给查询加锁：

```scala
val s = select (User.*) from User
s.forUpdate
```

在sqlserver中会在FROM子句后生成WITH (UPDLOCK)。

### 生成sql

上文的链式调用，只是构建出了sql语法树，并未真正生成sql语句，我们需要一个终止操作来生成sql：

```scala
given DB = DB.MYSQL

val s = select (User.*) from User orderBy User.id.asc limit 10

val sql: String = s.toSql
val countSql: String = s.toFetchCountSql
val pageSql: String = s.toPageSql(10, 1)
```

`toSql`会一比一的生成sql语句。

为了避免性能浪费，`toFetchCountSql`会拷贝语法树副本，把limit信息和order by信息去掉，并把select列表替换为COUNT(*)：

```sql
SELECT COUNT(*) AS `count`
FROM `user`
```

`toPageSql`，顾名思义，就是生成分页的查询，第一个参数为每页条数，第二个参数为页码，会依据这两个参数替换语法树副本的信息。

## union查询

支持`union`、`unionAll`、`except`、`exceptAll`、`intersect`、`intersectAll`来将两个查询拼接在一起：

```scala
val s1 = select (User.name) from User where User.id === 1
val s2 = select (User.name) from User where User.id === 2

val union = s1 union s2
```

用于拼接的查询，select中的字段数目与字段类型必须一致，否则无法通过编译。

## 原生sql

虽然构造器的功能已经十分完善，但可能有一些特殊的方言，比如postgresql中的distinct on，oracle中的connect by在构造器中没有对应的api，这时候也可以使用`sql""`构建原生sql：

```scala
val id = 1
val s = sql"select * from user where id = $id"
```

这看起来就像是原生的字符串模板一样，如果熟悉scala的字符串模板，将不会对这种方式感到陌生。

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
val i = insertInto(User)(User.*).values((1, "x"), (2, "y"))

val sql = i.toSql
```

values中的每个元组的类型必须与insertInto中传入的参数一致，否则会编译错误。

如果我们在上文元数据配置环节，将元数据对象写成实体类的伴生对象，那么就可以使用实体类来生成insert语句：

```scala
val user = User(1, Some("x"))
val i = insert(user)
val sql = i.toSql
```

**使用incr标记的主键会在生成sql时跳过。**

insert语句也可以使用子查询：

```scala
val i = insertInto(User)(User.*).select {
    select (User.*) from User
}
```

`select`方法中子查询的返回类型也需要与`insertInto`中的传参一致，这是`dependent type`的一种应用。

## 更新语句

库对非主键字段扩展了一个名为`to`的方法，这样我们就可以在更新语句中使用：

```scala
val u = update(User).set(User.name to "x").where(User.id === 1)
val sql = u.toSql
```

由于是对非主键类型的扩展，所以更新主键将不会通过编译。

to跟一般的运算一样，右侧不止可以是值，也可以是其他表达式，比如我们这样实现某字段自增的需求：

```scala
val u = update(A).set(A.x to A.x + 1)
```

与insert一样，同样可以使用实体类，来按主键更新其他字段：

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

// UPDATE entity SET b = 2 where a = 1
val u1 = update(e)

// UPDATE entity SET b = 2, c = null where a = 1
val u2 = update(e, skipNull = false)
```

## 删除语句

```scala
val d = deleteFrom(User).where(User.id === 1)
val sql = d.toSql
```

使用泛型传入实体类类型，便可以通过主键生成删除sql：

```scala
val d = delete[User](1)
val sql = d.toSql
```

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
val c = caseWhen(User.gender === 1 thenIs "男", User.gender === 2 thenIs "女") elseIs "其他"

select (c as "gender") from User
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
val c = caseWhen(User.gender === 1 thenIs User.gender) elseIs null

val select = select (count(c) as "male_count") from User
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
select (rank().over partitionBy User.id orderBy User.name.asc as "over") from User
```

这会产生如下的查询：

```sql
SELECT RANK() OVER (PARTITION BY user.id ORDER BY user.user_name ASC) AS over FROM user
```

`partitionBy()`接收若干个表达式类型

`orderBy()`接收若干个排序列，在表达式类型之后调用`.asc`或`.desc`来生成排序规则。

**窗口函数是一种高级查询方式，使用时需要注意数据库是否支持（比如mysql8.0以下版本不支持窗口函数功能）。**

### 自定义函数

除开内置的聚合函数外，我们还可以使用`NormalFunctionExpr`来创建普通函数，使用`AggFunctionExpr`来创建聚合函数，来看看下面的例子：

创建一个普通函数`LEFT`：

```scala
def left(e: Expr[String, _] | Expr[String | Null, _], size: Int): NormalFunctionExpr[String] = {
    import org.easysql.dsl.given
    NormalFunctionExpr("LEFT", List(e, size))
}

val s = select (User.*) from User where left(User.name, 3) === "xxx"
```

创建一个mysql的聚合函数GROUP_CONCAT：

```scala
def groupConcat(e: Expr[_, _], separator: String = "", distinct: Boolean = false, orderBy: OrderBy[_]*): AggFunctionExpr[String] = {
    import org.easysql.dsl.given
    AggFunctionExpr("GROUP_CONCAT", List(e), distinct, Map("SEPARATOR" -> separator), orderBy.toList)
}

val s1 = select (groupConcat(User.name, ",", true, User.id.asc)) from User

val s2 = select (groupConcat(User.name)) from User
```

这可能需要使用者对抽象语法树有一定的了解，如果你没有这种知识储备，那么也可以使用上文介绍的原生sql。

### cast转换

使用`cast`方法生成一个cast表达式用于数据库类型转换。

第一个参数为待转换的表达式；

第二个参数为String，为想转换的数据类型。

比如：

```scala
val select = select (cast(User.id, "CHAR")) from User
```

这会产生下面的查询：

```sql
SELECT CAST(user.id AS CHAR) FROM user
```

**会影响查询效率，不推荐使用**。

### with查询

`WithSelect()`生成一个with查询（mysql和pgsql使用递归查询在调用链添加.recursive）

```scala
val q1 = select (User.name) from User where User.id === 1 as "q1"
val q2 = select (User.name) from User where User.id === 2 as "q2"
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
val union = select (User.id, User.name) from User union (1, "x") union List((2, "y"), (3, "z"))
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
val insert = insertInto(User)(User.id, User.name).values((1, "x"), (2, "y"))
val result: Int = db.run(insert)
```

如果insert操作时，数据库中有自增主键，我们可以使用`runAndReturnKey`方法，返回一个List[Long]类型的结果集：

```scala
val insert = insertInto(User)(User.id, User.name)((1, "x"), (2, "y"))
val result: List[Long] = db.runAndReturnKey(insert)
```

## 接收查询结果

对于select、values临时表等查询类sql，可以使用`JdbcConnection`类进行查询，并返回查询结果。

支持的单条结果映射类型有三种：

1. 映射到继承了TableEntity的实体类（**可空字段将会被映射到Option类型**）；
2. 映射到一个Tuple，Tuple的实际类型取决于select方法的参数（**此时不能使用select \*或者无类型参数的col，否则会出现运行时异常**）；
3. 映射到一个Map[String, Any]，map的key为字段名（或查询中的别名），value为查询结果的值。

### 查询结果集

使用`queryMap`、`queryTuple`、`queryEntity`来查询结果集，返回结果是一个`List`，如果没有查询到结果，返回一个0元素的List：

```scala
val select = select (User.id, User.name) from User

val result1: List[Map[String, Any]] = db.queryMap(select)
val result2: List[(Int, String | Null)] = db.queryTuple(select)
val result3: List[User] = db.queryEntity[User](select)
```

### 查询单条结果

使用`findMap`、`findTuple`、`findEntity`来查询单条结果，返回结果是一个`Option`，如果没有查询到结果，返回一个None：

```scala
val select = select (User.id, User.name) from User

val result1: Option[Map[String, Any]] = db.findMap(select)
val result2: Option[(Int, String | Null)] = db.findTuple(select)
val result3: Option[User] = db.findEntity[User](select)
```

### 分页查询

使用`pageMap`、`pageTuple`、`pageEntity`来进行分页查询，返回结果是一个`Page`类型，其定义如下：

```scala
case class Page[T](totalPage: Int = 0, totalCount: Int = 0, data: List[T] = List())
```

分页查询参数中除了需要传入查询dsl之外，还需要依次传入一页的结果集条数，页数，和是否需要查询count；

其中最后一个参数，默认值为true，为true时会附带执行一个查询count的sql，如无必要，请传入false，以便提升效率：

```scala
val select = select (User.id, User.name) from User

val result1: Page[Map[String, Any]] = db.pageMap(select)(10, 1)
val result2: Page[(Int, String | Null)] = db.pageTuple(select)(10, 1, true)
val result3: Page[User] = db.pageEntity[User](select)(10, 1, false)
```

### 查询count

使用`fetchCount`方法来查询结果集大小，返回结果是`Int`类型。

**此处会对生成的sql语法树进行复制，并去除对于查询count无用的order by和limit信息，并把select列表替换成COUNT(*)，以提高查询效率**：

```scala
val select = select (User.id, User.name) from User orderBy User.id.asc limit 10 offset 10

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

scala最好的orm：quill的核心作者，在此感谢jilen指引我前进的方向。

氘発孜然：https://github.com/daofaziran1

在此感谢氘発孜然在scala3的macro等方面给予我的帮助。
