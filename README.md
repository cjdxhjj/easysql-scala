# 项目介绍

easysql-scala是一个使用Scala3编写的完全面向对象的sql生成框架，也提供了一定程度的查询映射支持。

我们可以使用接近于原生sql的dsl构建跨数据库的sql语句，**无需任何代码以外的配置，就能构造出复杂的查询**，比如：

```scala
val select = (select (User.*, Post.*)
        from User 
        leftJoin Post on User.id === Post.uid
        orderBy User.id.asc
        limit 10 offset 10)
```

使用这种dsl，有下面的好处：
1. 类型安全：将表达式与错误类型的值或者表达式比较，更新主键等场景，将会编译失败；
2. 防注入：dsl的背后是sql语法树，并非是单纯字符串拼接，静态的语法树类型可以防止绝大多数sql注入；
3. 查询的任何部分都可以被封装到方法或变量中，我们可以用来动态构建sql；
4. 对于同一套查询，使用不同的数据库枚举值，就可以方便地生成不同数据库的sql；
5. 获得ide提示。


支持mysql、postgres sql、oracle、sqlserver、sqlite在内的多种数据库，并且封装出了统一的api。（**mysql与pgsql为第一优先级支持**）

# 快速开始

我们编写一个`object`，继承`TableSchema`，例如：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id: TableColumnExpr[Int] = intColumn("id")
    val name: TableColumnExpr[String] = varcharColumn("name")
}
```

在伴生对象中添加`override val tableName: String`，其值为表名。

给伴生对象的属性赋值成column()类型，在column()函数中添加数据库列名。

column类型支持：`intColumn`、`longColumn`、`varcharColumn`、`floatColumn`、`doubleColumn`、`booleanColumn`、`dateColumn`。

然后我们就可以使用`select`方法创建一个`Select`实例，并编写一个简单的查询：

```scala
val s = select (User.id) from User
```

上面代码中的`User`和`User.id`即是来自我们定义好的`object`，藉此我们可以获得类似原生sql的编写体验。


# 元数据配置

我们对上文的`object`加以改造，对主键字段填加`.primaryKey`调用（**此时字段类型将不再是TableColumnExpr，而是PrimaryKeyColumnExpr**）。

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").primaryKey
    val name: TableColumnExpr[String] = varcharColumn("name")
}
```

如果是自增主键，使用`.incr`（**一张表支持多个主键但只支持一个自增主键**）：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").incr
    val name: TableColumnExpr[String] = varcharColumn("name")
}
```

对可空类型的字段，我们添加`.haveNull`，并在类型参数中添加（| Null）：

```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").incr
    val name: TableColumnExpr[String | Null] = varcharColumn("name").haveNull
}
```

为了更方便的使用，我们可以添加一个展开所有字段的方法，把返回值定义成字段元组：
```scala
object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").incr
    val name: TableColumnExpr[String | Null] = varcharColumn("name").haveNull
    def * = (id, name)
}
```

这样就可以在查询中使用`User.*`，便能在生成sql时自动展开查询字段。

后续的查询构造器中，将会利用`object`中的配置，进行类型检查。

如果你使用的是mysql或者postgresql数据库，可以使用`jdbc`子项目中的工具来自动生成这些样板代码:

```scala
// 此处省略连接池配置
val ds: DataSource = ???
// 创建一个jdbc连接，传入数据库类型和连接池
val db = new JdbcConnection(DB.MYSQL, ds)
// 传入数据库名称和生成路径，来生成代码
db.generateEntity("postgres", "src/main/scala/codegen/")
```

根据实际需要进行微调即可。

# 查询构造器

接下来我们将开始了解查询构造器，为了更好地使用查询构造器，也为了使用者更深入的了解sql的本质，我们先来了解表达式和运算符：

## 表达式和运算符

我们首先来介绍库提供的表达式和各种运算符。

表达式拥有共同的父类`Expr`，而大多数表达式的参数中也可以接收`Expr`类型，因此，表达式之间可以互相嵌套，便可以提供高抽象能力。

**表达式类型中如果代入进了字符串，会在生成sql时自动转义特殊符号，以此来防止sql注入。**

### 字段

字段是最基本的表达式，比如上文`object`中的属性`User.id`就是一个字段类型，可以代入到其他表达式或查询语句中：

```scala
select (User.id) from User
```

如果我们需要查询全部字段，那么可以像下面这样调用：

```scala
select (**) from User
```

**有些遗憾的是，由于与乘法运算符冲突，所以此处不能像真正的sql一样使用单个星号**。

这个表达式的定义如下：
```scala
def ** = AllColumnExpr()
```

当然，我们使用上文定义的全部字段的元组，就不存在这个问题：
```scala
select (User.*) from User
```

假如你的项目中，表名字段名是动态的，并不能构建出实体类，那可以使用另一种方式生成字段表达式：

```scala
col[Int]("c1")
```

其中的类型参数为字段的实际类型（**也可以不传类型参数，但这会失去类型安全性，请使用者自行斟酌**），可空字段可以写成：
```scala
col[Int | Null]("c1")
```

将这种字段代入查询：

```scala
select (col[Int]("c1")) from User
```

如果`col()`中的字符串包含`.`，那么`.`左侧会被当做表名，右侧会被当做字段名；如果包含`*`，那么会产生一个sql通配符。

### 表达式别名

表达式类型可以使用中缀函数`as`来起别名，我们在此以字段类型为例，后文的其他表达式类型也支持这个功能：

```scala
select(User.id as "c1") from User
```

### 常量

在某些需求中，可能会将某一种常量来作为查询结果集的一列，比如：

```sql
SELECT 1 AS c1
```

我们可以使用`const()`来生成常量类型的表达式：

```scala
select(const(1) as "c1")
```

当然，利用Scala强大的编译器，只要在使用时**隐式转换**一下，此处就可以省略掉`const()`：

```scala
import org.easysql.dsl.given
select(1 as "c1")
```

隐式转换后也可以在运算符左侧使用此值：
```scala
import org.easysql.dsl.given
select (**) from User where 1 < User.id
```

### 聚合函数

内置了`count`、`countDistinct`、`sum`、`avg`、`max`、`min`这些标准的聚合函数，比如：

```scala
select (count() as "col1", sum(User.id) as "col2") from User
```

### 逻辑运算符

库内置了`==`、`===`（**由于==与内置库函数同名，所以使用==可能在某些情况下会产生预期之外的结果，所以更推荐使用===代替==**）、`<>`、`>`、`>=`、`<`、`<=`、`&&`(AND)、`||`(OR)、`^`(XOR)等逻辑运算符，我们可放入where条件中：

```scala
select (User.*) from User where User.id === 1
```

运算符们可以自由组合：

```scala
select (User.*) from User where User.name === "小黑" && (User.id > 1 || User.gender === 1)
```

如果使用`AND`来拼接条件，也可以使用多个`where`。

除了上文的逻辑运算符外，还支持`in`、`notIn`、`like`、`notLike`、`between`、`notBetween`、`isNull`、`isNotNull`：

```scala
select(User.*)
    .from(User)
    .where(User.gender in (1, 2))
    .where(User.id between (1, 10))
    .where(User.name like "%xxx%")
    .where(User.name.isNotNull)
```

上文中`object`里未使用`haveNull`来标注的字段，使用`isNull`或`isNotNull`时，产生一个**编译错误**。

这些运算符不仅可以代入数值，字符串等常量，表达式类型的子类也可以代入其中，比如我们需要做一个需求，查询当前的时间在表的两个字段范围内的数据，我们可以这样写：

```scala
select (User.*) from User where const(Date()).between(User.time1, User.time2)
```

这已经体现出运算符的抽象能力了，但是，我们还可以再简洁一些：

```scala
import org.easysql.dsl.given
select (User.*) from User where Date().between(User.time1, User.time2)
```

### 数学运算符

库提供了`+`、`-`、`*`、`/`、`%`五个数学运算符，比如：

```scala
select (User.id + 1) from User
```

### case表达式

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

使用`聚合函数`或`rank()`、`denseRank()`、`rowNumber()`三个窗口专用函数，在后面调用`.over`，来创建一个窗口函数，然后通过`partitionBy`和`orderBy`来构建一个窗口：

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

### 普通函数

可以使用内置的`NormalFunctionExpr`来封装一个数据库函数。

**函数不利于查询优化以及不同数据库转换，因此不推荐使用**。

### cast表达式

使用`cast()`方法生成一个cast表达式用于数据库类型转换。

第一个参数为表达式类型，为待转换的表达式；

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

## 查询语句

在介绍完表达式和运算符之后，我们便可以开始着重来讲sql的核心：`select`语句的构建。

库内置了一系列方法来支持SELECT语句的编写，比如：

```scala
val select = select (User.*) from User where User.name === "小黑"
```

然后我们就可以用`sql`方法，并传入数据库类型枚举`DB`，获取生成的sql：

```scala
val sql = select.sql(DB.MYSQL)
```

当然，生成sql语句的方法并非只有sql，后文会详细说明。

### select子句

select()方法中支持传入若干个前文介绍的表达式类型：

```scala
select(User.id, User.name)
```

链式调用多个`select`，会在生成sql时依次拼接进sql语句。

由于需要对查询类型校验，每次调用`select`都会产生一个新的`Select`对象，如无必要，请**慎用**多次select。

上面的`select`，对于union和子查询是**类型安全**的，但是查询字段需要在编译期确定，
虽然安全，但在select的字段列表在运行期才能确定时并不方便，在这种动态sql的场景里，我们可以使用`dynamicSelect`：

```scala
// 假设这个List的内容是运行期用户传参
val list = List("x", "y", "z") 

// 将字符串列表映射成字段列表，传入dynamicSelect的可变参数中
val select = dynamicSelect(list.map(col): _*) from "table1" 
```

这样虽然失去了类型安全，但在高度动态的查询里，使用更加简单。

使用哪种方式需要根据使用者的实际业务场景决定，如非必要，不推荐使用`dynamicSelect`。

### from子句

`from()`方法支持传入一个字符串表名，或者前文介绍的继承了`TableSchema`的对象名：

```scala
select (User.*) from User

select (**) from "table"
```

此功能使用Scala3的union type功能实现。

**不支持from多张表，如果有此类需求，请使用join功能。**

### 表别名

通常需要对表起别名时，分成两个场景，自连接和子查询，下面我们将会对这两种情况一一进行说明。

自连接时，使用`TableSchema`的`as`方法，创建一个新的表实例：

```scala
val t1 = User as "t1"
val t2 = User as "t2"

select (**) from t1 join t2 on t1.id === t2.id where t1.name === "小黑"
```

对于动态表名，我们可以使用`table()`括起来，再使用`as`方法。

**需要注意的是，给表起别名之后，便不能使用元数据中定义的展开字段元组的方法。**

子查询时，使用`Select`类的`as`方法，修改别名：

```scala
val sub = (select (User.id as "c1", User.name as "c2") from User) as "sub"
select (sub.c1) from sub where sub.c2 === "小黑"
```

子查询的表，需要手动展开查询字段，**并对每个字段起别名**，在下面引述子查询的时候，便可以通过别名字符串推断出属性名。

### where子句

使用`where()`配合各种前面介绍的运算符和表达式，生成where条件：

```scala
select (User.*) from User where User.id === 1 && User.gender <> 1
```

多个`where()`会使用AND来拼接条件，如果需要使用OR和XOR，请参考前文的运算符部分。

有些时候，我们需要根据一些条件动态拼接where条件，我们可以这样调用：

```scala
select(User.*).from(User).where(testCondition, User.name === "")
```

`where()`的第一个参数接收一个Boolean表达式，只有表达式返回的值是true的时候，条件才会被拼接到sql中。

如果判断条件比较复杂，第一个参数也可以传入一个返回Boolean类型的lambda表达式。

**不止是select语句，后文的update和delete语句也支持这些where()的调用方式，以后便不再赘述。**

### order by子句

使用`orderBy()`方法传入表达式类型的`.asc`或者`.desc`方法来生成的排序规则：

```scala
select (User.*) from User orderBy (User.id.asc, User.name.desc)
```

这可能还不够像sql风格，我们可以导入`scala.language.postfixOps`来去掉asc和desc之前的点:
```scala
import scala.language.postfixOps
select (User.*) from User orderBy (User.id asc, User.name desc)
```

### group by和having子句

使用`groupBy()`来聚合数据（多个group by字段需要用小括号括起来），`having()`来做聚合后的筛选：

```scala
select (User.gender, count()) from User groupBy User.gender having count() > 1
```

`groupBy()`接收若干个表达式类型，`having()`的使用方式与`where()`相似。

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
   
### distinct子句

在调用链中添加`distinct`即可，会对查出的列进行去重。

```scala
select(User.name).from(User).distinct
```

### limit子句

使用`limit(count, offset)`来做数据条数筛选（**注意此处与mysql的参数顺序不一样**），如：

```scala
select(User.*).from(User).limit(1, 100)
```

limit中第二个参数也可以不填，即为默认值0：

```scala
select(User.*).from(User).limit(1)
```

我们也可以使用中缀函数limit和offset组合调用

```scala
select (User.*) from User limit 1 offset 10
```

也可以不调用offset函数，即为默认值0。

**limit语句并不是sql标准用法，因此每个数据库厂商采用的语法都有差异，生成sql时会根据数据源的数据库类型进行方言适配。**

**oracle需要版本在12c以上，sqlserver需要版本在2012以上。低于此版本，需要使用者自行处理ROW NUMBER。**

### join子句

提供：`join()`、`innerJoin()`、`leftJoin()`、`rightJoin()`、`crossJoin()`、`fullJoin()`几种不同的join方法。

上述方法配合`on()`方法来做表连接：

```scala
select (User.*, Post.*) from User leftJoin Post on User.id === Post.uid
```

支持自定义join的顺序，比如：
```scala
select (**) from A leftJoin (B join C on B.col === C.col) on A.col === B.col
```

小括号里面依然可以继续嵌套小括号，解析时将对join结构进行递归处理。

### 子查询

如果需要使用子查询，我们另外声明一个`Select`对象传入调用链即可：

```scala
val sub = (select (User.*) from User) as "t1"

select (**) from sub
```

join中的子查询：

```scala
val sub = (select (Post.userId as "id") from Post limit 10) as "t1"

select (**) from User leftJoin sub on User.id === sub.id
```

**子查询的别名规则请参考上文的表别名说明**。

操作符中的子查询：

```scala
select(User.*) from User where (User.id in select(User.id).from(User).limit(10))
````

支持`exists`、`notExists`、`any`、`all`、`some`这五个子查询谓词，使用对应的全局函数把查询调用链代入即可：

```scala
select (User.*) from User where exists(select (max(User.id)) from User)
```

当然子查询谓词依然是表达式类型，所以可以使用操作符函数来计算：

```scala
select (User.*) from User where User.id < any(select (max(User.id)) from User)
```

如果需要使用LATERAL子查询，把`from()`改为`fromLateral()`即可（join的调用方式类似，**需要注意使用的数据库版本是否支持LATERAL关键字**）：

### for update

使用`forUpdate`方法将查询加锁：

```scala
select(User.*).from(User).forUpdate
```

不支持sqlite；在sqlserver中会在FROM子句后生成WITH (UPDLOCK)；其他数据库会在sql语句末尾生成FOR UPDATE。

### 获取sql

前面通过链式调用构建的查询，其实只是构建出sql语法树，还并未生成sql，我们还需要一个链式调用终止操作，来生成需要的sql语句：

```scala
val s = select (User.*) from User

s.sql(DB.MYSQL) // 传入数据库枚举，直接生成sql

s.fetchCountSql(DB.MYSQL) // 生成查询count的sql（会复制查询副本并去掉limit和order by信息）

s.pageSql(10, 1)(DB.MYSQL) // 生成分页sql（会复制查询副本并根据参数替换掉limit信息）
```

在只使用一种数据库的情况下，上面这种每次生成sql都传入一个数据库类型的方式略显繁琐，我们可以定义一个`given`，来帮我们**隐式传入**数据库类型：
```scala
given DB = DB.MYSQL

s.toSql
s.toFetchCountSql
s.toPageSql(10, 1)
```

在隐式值的作用域下，在生成sql方法名前面添加to，我们就可以省略掉数据库类型传参。

如果你需要同时使用多种数据库，那么还是推荐使用第一种方式。

## 其他查询语句

除了普通的select语句之外，还支持union等一些特殊查询，**这些查询只支持使用sql方法获取sql语句**：

### union查询

支持`union`、`unionAll`、`except`、`exceptAll`、`intersect`、`intersectAll`来将两个查询拼接在一起：

```scala
val s = (select (User.name) from User where User.id === 1) union (select (User.name) from User where User.id === 2)
```

如果两个查询select中的返回类型不一致，**将无法通过编译。**

### with查询

`WithSelect()`生成一个with查询（mysql和pgsql使用递归查询在调用链添加.recursive）

```scala
import org.easysql.dsl.given

val w = WithSelect()
    .add("q1", List("name"), Select() select User.name from User where User.id === 1)
    .add("q2", List("name"), Select() select User.name from User where User.id === 2)
    .select { s =>
        s from "q1" join "q2" on true
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

## 原生sql

虽然内置的sql功能已经很全面，但仍然有可能不满足使用者需求，
这种情况下，我们可以使用`sql`字符串插值器调用原生sql：

```scala
val id = 1
val name = "xxx"
val s = sql"select * from user where id = $id and name = $name"
```

字符串等类型会在生成sql语句时自动生成单引号。

**此方式失去了类型安全性，如无必要，不推荐使用。**

## 插入语句

```scala
val i = insertInto(User)(User.id, User.name).values((1, "x"), (2, "y"))
val sql = i.sql(DB)
```

当然，此处也可以使用之前定义好的字段元组：

```scala
val i = insertInto(User)(User.*).values((1, "x"), (2, "y"))
val sql = i.sql(DB)
```

得益于Scala3强大的类型系统，**values中如果类型与前面的字段定义不一致，将会产生编译错误。**

### 使用实体类

我们也可以使用实体类来插入数据，`case class`继承`TableEntity`, 并添加伴生对象：

```scala
case class User(id: Int = 123, name: Option[String] = Some("")) extends TableEntity[Int]

object User extends TableSchema {
    override val tableName: String = "user"
    val id: PrimaryKeyColumnExpr[Int] = intColumn("id").incr
    val name: TableColumnExpr[String | Null] = varcharColumn("user_name").haveNull
    def * = (id, name)
}
```

**TableEntity中的类型参数为主键类型，如果是联合主键，可以传入一个元组类型，比如：TableEntity[(Int, String)]**

**联合主键的表，请确保实体类、伴生对象、TableEntity的主键定义顺序一致。**

然后使用`insert`方法：
```scala
val user = User(name = Some("x"))
val sql = insert(user).sql(DB)
```

使用`incr`标注的字段将会在语句生成时忽略。

### 子查询

当然，插入语句中也支持使用子查询：

```scala
val i = insertInto(User)(User.id, User.name).select {
    select (User.id, User.name) from User
}
```

此处会对`insert`指定的列和`select`中的列进行类型匹配检查，如不相符，则会**编译错误。**

框架合理利用了Scala的类型系统，达到状态安全，对一个`Insert`同时对象调用`values`和`select`方法时，将会**编译错误**：

```scala
val i = insertInto(User)(User.id, User.name).values((1, "x"), (2, "y"))

// 编译错误，一个Insert对象只能调用values或select的其中一个方法
i.select {
    select (User.id, User.name) from User
}
```

## 更新语句

```scala
val u = update (User) set (User.name to "x") where User.id === 1
```

`to`是对`TableColumnExpr`添加的扩展函数，其中添加了类型检查，所以**如果更新成与字段不同的类型，将不会通过编译检查**；

并且由于是对`TableColumnExpr`的扩展，所以如果**更新主键，将会产生编译错误**。

`to`的右边不止可以是值，也可以是其他表达式，所以我们可以实现这样的功能：

```scala
update (User) set (User.followers to User.followers + 1) where User.id === 1
```

我们也可以传入上文插入语句介绍中的实体类，用主键字段的值作为条件更新其他字段：

```scala
val user = User(1, Some("x"))
update(user)
```

## 删除语句

我们这样创建一个删除语句：

```scala
val d = deleteFrom (User) where User.id === 1
```

我们也可以使用实体类的类型生成按主键删除的sql：

```scala
delete[User](1)
```

## 插入或更新

使用实体类生成按主键插入或更新的sql：
```scala
val user = User(1, Some("x"))
val s = save(user)
val sql = s.sql(DB)
```

**每一种数据库生成的sql均不同。**

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
db.transaction(TRANSACTION_READ_UNCOMMITTED, { t =>
    t.run(...)
})
```

# 致谢

easysql的scala版本的诞生，需要感谢两个人：

jilen：https://github.com/jilen

scala最好的orm：quill的核心作者，在此感谢jilen指引我前进的方向。

氘発孜然：https://github.com/daofaziran1

在此感谢氘発孜然在scala3的macro等方面给予我的帮助。
