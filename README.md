# 电商平台小组项目

这是一个使用 Java 编写的电商平台课程项目，计划用八周逐步完成。当前进度为**第2周第1次课**，已完成控制台商品原型、商品服务拆分和 MySQL 基础表设计。

## 第一周已实现

- 启动程序并显示控制台主菜单。
- 浏览四个示例商品，显示商品编号、名称、价格和库存。
- 输入商品编号查看详情，输入 0 返回。
- 对不存在的商品编号和无效菜单输入给出提示。
- 查看项目简介和后续开发范围。
- 正常退出程序，或在输入结束后退出。

控制台通过 `ProductService` 读取内存中的示例商品。第2周第1次课新增了独立的 MySQL 初始化脚本。第2周第2次课计划使用 JDBC 接入商品查询。

## 技术规划

项目整体计划使用 Java、JDBC 和 MySQL。Java 负责控制台交互及业务处理，JDBC 负责访问数据库，MySQL 负责保存业务数据。第一周仅使用 JDK 标准库，先建立可运行的基础原型，再逐步增加数据库访问层与业务层。

## 小组分工

| 姓名 | 角色 | 负责方向 |
| --- | --- | --- |
| 宋思远 | 项目经理 | 需求梳理、任务安排与进度协调 |
| 黄一飞 | 测试 | 检查操作流程、异常输入与问题反馈 |
| 李世君 | 后端开发 | 用户与账户模块 |
| 何雨轩 | 后端开发 | 商品、购物车与库存模块 |
| 李琬瑶 | 后端开发 | 订单、会员与促销模块 |

## 本地运行

需要安装 JDK 17 或以上版本，并确保 `java` 和 `javac` 可以在命令行使用。本次在 JDK 21 下编译运行通过。

Windows 下双击 `start.bat`。也可以在项目根目录运行：

```powershell
javac -encoding UTF-8 -sourcepath src -d build/classes src/com/ecshop/App.java
java '-Dfile.encoding=UTF-8' '-Dstdin.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp build/classes com.ecshop.App
```

菜单示例：

```text
电商平台控制台系统 第一周原型
1. 浏览商品
2. 项目介绍
0. 退出系统
```

## 文件说明

```text
src/com/ecshop/App.java                 程序入口
src/com/ecshop/console/ConsoleUI.java   控制台菜单与示例商品浏览
src/com/ecshop/model/Product.java       商品实体
src/com/ecshop/service/ProductService.java  示例商品列表与查询
docs/requirements.md                   需求与八周开发计划
docs/week01.md                         第一周工作说明
docs/manual-checks.md                  基础操作检查清单
sql/schema.sql                         第2周基础表与示例数据
start.bat                              Windows 编译启动入口
```

## 第2周第1次课数据库实验

在 MySQL 客户端中执行 `sql/schema.sql`。脚本使用独立的 `xiaozuzuoye_week02` 数据库，建立 `member_level`、`app_user`、`product` 三张表，并写入示例会员等级和商品。执行账号需要建库、建表和插入数据权限。可用 `SHOW TABLES;` 和 `SELECT id, name, price, stock FROM product ORDER BY id;` 检查结果。

两次课的记录合并在 [阶段实验报告](docs/第01周第02次课与第02周第01次课_合并实验报告.docx)。

## 下一步

第2周第2次课学习 JDBC，逐步把内存中的商品数据替换为数据库查询结果。

