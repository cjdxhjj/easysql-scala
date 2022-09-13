# 概述

easysql是一个使用Scala3编写的sql构造器，其充分利用了Scala3优秀的类型系统，可以在编译期解决掉绝大多数的错误sql。并且，得益于Scala强大的表达能力，api与原生sql非常相似，极大降低了学习成本。

虽然本身定位并非orm，但完全可以把它当做轻量级的orm使用，比如执行查询，并把结果映射到类（支持多表），或是使用case class直接生成insert、update等语句，避免样板代码。（以上功能使用内联函数和宏实现，而非运行期反射，接近0开销）。


项目文档：https://wz7982.github.io/easysql-doc
