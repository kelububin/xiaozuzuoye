package com.ecshop.db;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * 数据库访问层（MySQL 版）
 *
 * 通过 lib/mysql-connector-java-8.0.28.jar 驱动连接本机 MySQL 的 order_buy 库。
 * 优先从类路径加载驱动（IDEA 中已把 jar 加入依赖时直接生效）；
 * 若类路径上没有驱动，则回退为从 lib 目录下的 jar 文件动态加载。
 */
public final class Database {

    // Local settings; JVM -D properties take precedence.
    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    private static Properties loadLocalConfig() {
        Properties p = new Properties();
        Path path = Path.of("database.properties");
        if (Files.exists(path)) {
            try (var in = Files.newInputStream(path)) { p.load(in); }
            catch (java.io.IOException e) { throw new RuntimeException("Cannot read database.properties", e); }
        }
        return p;
    }

    private static String setting(String key, String fallback) {
        return System.getProperty(key, LOCAL_CONFIG.getProperty(key, fallback));
    }

    // ---------- 连接配置（可用 -D 参数覆盖） ----------
    private static final String HOST = setting("db.host", "127.0.0.1");
    private static final int PORT = Integer.parseInt(setting("db.port", "3306"));
    private static final String DB_NAME = setting("db.name", "xiaozuzuoye_week02");
    private static final String USER = setting("db.user", "xiaozuzuoye_app");
    private static final String PASS = setting("db.pass", "");

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME.replace(" ", "%20")
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                    + "&createDatabaseIfNotExist=true&characterEncoding=utf8";

    /** MySQL 驱动 jar 路径（类路径缺失驱动时回退使用，可用 -Ddb.driver.jar=路径 覆盖） */
    private static final String MYSQL_DRIVER_JAR =
            System.getProperty("db.driver.jar",
                    "lib/mysql-connector-java-8.0.28.jar");

    private static Database INSTANCE;
    private final Connection conn;

    private Database() {
        try {
            Driver driver = loadDriver();
            Properties info = new Properties();
            info.setProperty("user", USER);
            info.setProperty("password", PASS);
            info.setProperty("characterEncoding", "utf8");
            this.conn = driver.connect(URL, info);
            if (this.conn == null) {
                throw new SQLException("MySQL 驱动无法建立连接: " + URL);
            }
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
        }
    }

    /** 加载 MySQL 驱动：先试类路径，失败则从 lib 下的 jar 动态加载 */
    private static Driver loadDriver() {
        // 1) 类路径上已有驱动（IDEA 已添加依赖）时直接使用
        try {
            Class<?> cls = Class.forName("com.mysql.cj.jdbc.Driver");
            return (Driver) cls.getDeclaredConstructor().newInstance();
        } catch (Throwable ignored) {
            // 继续走 jar 动态加载
        }
        // 2) 从 lib/mysql-connector-java-8.0.28.jar 动态加载，
        //    用 driver.connect() 直连，绕开 DriverManager 的类加载器可见性限制
        try {
            File jar = new File(MYSQL_DRIVER_JAR);
            if (!jar.exists()) {
                throw new RuntimeException("未找到 MySQL 驱动文件: " + MYSQL_DRIVER_JAR
                        + "，请确认 jar 已加入项目依赖或路径正确");
            }
            URL jarUrl = jar.toURI().toURL();
            URLClassLoader cl = new URLClassLoader(new URL[]{jarUrl}, Database.class.getClassLoader());
            Class<?> cls = cl.loadClass("com.mysql.cj.jdbc.Driver");
            return (Driver) cls.getDeclaredConstructor().newInstance();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("加载 MySQL 驱动失败: " + e.getMessage(), e);
        }
    }

    public static synchronized Database getInstance() {
        if (INSTANCE == null) INSTANCE = new Database();
        return INSTANCE;
    }

    public Connection getConnection() {
        return conn;
    }

    // ---------- 初始化 ----------

    private void initSchema() throws Exception {
        // 建库 + 建表 + 种子数据已全部合并在 schema.sql 中，一次性执行即可
        if (!DB_NAME.matches("[A-Za-z0-9_]+")) throw new IllegalArgumentException("数据库名称只能包含字母、数字和下划线");
        runScript(Files.readString(Path.of("sql", "schema.sql")).replace("`xiaozuzuoye_week02`", "`" + DB_NAME + "`"));

    }

    /** 逐条执行 SQL 脚本：去掉 -- 注释行，按 ; 拆分（MySQL 不支持单次执行多语句） */
    private void runScript(String sql) throws SQLException {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\n", -1)) {
            if (line.trim().startsWith("--")) continue;
            cleaned.append(line).append('\n');
        }
        for (String part : cleaned.toString().split(";")) {
            String stmt = part.trim();
            if (stmt.isEmpty()) continue;
            try (Statement st = conn.createStatement()) {
                st.execute(stmt);
            }
        }
    }

    // ---------- 常用操作 ----------

    /** 执行更新，返回受影响行数 */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /** 执行插入并返回自增主键 */
    public long insertAndGetKey(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                return -1;
            }
        }
    }

    /** 查询多条 */
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper.map(rs));
            }
        }
        return list;
    }

    /** 查询单条，无结果返回 null */
    public <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper.map(rs);
                return null;
            }
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object v = params[i];
            if (v == null) ps.setNull(i + 1, Types.NULL);
            else if (v instanceof String s) ps.setString(i + 1, s);
            else if (v instanceof Integer n) ps.setInt(i + 1, n);
            else if (v instanceof Long n) ps.setLong(i + 1, n);
            else if (v instanceof Double n) ps.setDouble(i + 1, n);
            else if (v instanceof Boolean b) ps.setBoolean(i + 1, b);
            else if (v instanceof java.time.LocalDateTime t) ps.setTimestamp(i + 1, Timestamp.valueOf(t));
            else ps.setObject(i + 1, v);
        }
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** 在事务中执行（共享连接，串行化以保证事务完整性，演示用） */
    public synchronized <T> T tx(Callable<T> work) {
        boolean prev;
        try {
            prev = conn.getAutoCommit();
        } catch (SQLException e) {
            prev = true;
        }
        if (!prev) {
            try { return work.call(); }
            catch (RuntimeException e) { throw e; }
            catch (Exception e) { throw new RuntimeException(e); }
        }
        try {
            conn.setAutoCommit(false);
            T r = work.call();
            conn.commit();
            return r;
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            if (e instanceof RuntimeException re) throw re;
            throw new RuntimeException(e);
        } finally {
            try { conn.setAutoCommit(prev); } catch (SQLException ignore) {}
        }
    }
}
