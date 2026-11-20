# 电商平台小组项目

当前进度为第4周第2次课。程序使用 Java 控制台、JDBC 和 MySQL，实现商品浏览、用户注册、密码与邮箱验证码登录，以及购物车查看、添加、修改、移除和清空。商品加入购物车时预留库存，数量减少、移除或清空时回补库存，相关更新使用数据库事务。

## 本地运行

1. 安装 JDK 17 或以上版本，启动本机 MySQL。
2. 将 `database.properties.example` 复制为 `database.properties`，填写本机数据库账号和密码。默认数据库名为 `xiaozuzuoye_week02`。账号需要创建数据库、建表及读写数据的权限，也可以提前创建数据库并调整权限。
3. 双击 `start.bat`。程序启动时执行 `sql/schema.sql` 初始化表及示例数据。
4. 首次使用先注册，再登录。本地未配置邮件服务时，验证码显示在控制台窗口。

命令行启动：

```powershell
javac -encoding UTF-8 -sourcepath src -d build/classes src/com/ecshop/App.java
java '-Dfile.encoding=UTF-8' '-Dstdin.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'build/classes;lib/mysql-connector-java-8.0.28.jar' com.ecshop.App
```

## 本阶段功能

- 商品列表及详情从 MySQL 查询。
- 注册检查用户名、密码及邮箱，登录使用密码和验证码。
- 购物车按用户区分，同一商品重复添加时合并数量。
- 修改数量按差额调整库存，移除和清空时释放预留库存。
- 加购、修改、移除和清空的购物车与库存更新在事务中完成。

下一阶段计划实现订单及结算。密码和本地配置应保存在 `database.properties`，该文件已加入 `.gitignore`。

## 成员分工

宋思远负责项目管理，黄一飞负责测试，李世君、何雨轩和李琬瑶负责后端开发。
