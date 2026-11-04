# 电商平台小组项目

这是一个使用 Java 编写的电商平台课程项目，计划用八周逐步完成。本次提交为**第一周基础原型**，重点是需求分析、小组分工与控制台入口。

## 第一周已实现

- 启动程序并显示控制台主菜单。
- 浏览四个示例商品，显示商品编号、名称、价格和库存。
- 输入商品编号查看详情，输入 0 返回。
- 对不存在的商品编号和无效菜单输入给出提示。
- 查看项目简介和后续开发范围。
- 正常退出程序，或在输入结束后退出。

本周商品数据直接保存在内存中，用于验证界面流程。数据库、注册登录、购物车、订单和会员促销属于后续周次的开发内容，当前版本尚未实现。

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
start.bat                              Windows 编译启动入口
```

## 下一步

第二周计划设计数据库表，学习 JDBC，逐步把内存中的商品数据替换为数据库查询结果。

