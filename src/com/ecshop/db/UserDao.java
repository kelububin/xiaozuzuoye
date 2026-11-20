package com.ecshop.db;

import com.ecshop.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class UserDao {
    private final Database db = Database.getInstance();

    public User findByUsername(String username) throws SQLException {
        return db.queryOne(
            "SELECT id,username,password_hash,email,member_level_id,points," +
            "failed_attempts,locked,created_at FROM app_user WHERE username=?",
            this::map, username);
    }

    public User findById(long id) throws SQLException {
        return db.queryOne(
            "SELECT id,username,password_hash,email,member_level_id,points," +
            "failed_attempts,locked,created_at FROM app_user WHERE id=?",
            this::map, id);
    }

    public List<User> findAll() throws SQLException {
        return db.query(
            "SELECT id,username,password_hash,email,member_level_id,points," +
            "failed_attempts,locked,created_at FROM app_user ORDER BY id",
            this::map);
    }

    public long insert(String username, String passwordHash, String email)
            throws SQLException {
        return db.insertAndGetKey(
            "INSERT INTO app_user(username,password_hash,email) VALUES(?,?,?)",
            username, passwordHash, email);
    }

    /** 登录失败：累加失败次数，达到阈值则冻结 */
    public void recordLoginFailure(long id, int failedAttempts, boolean locked)
            throws SQLException {
        db.update("UPDATE app_user SET failed_attempts=?, locked=? WHERE id=?",
                failedAttempts, locked, id);
    }

    /** 登录成功或管理员解锁：清空失败计数并解冻 */
    public void resetLoginFailure(long id) throws SQLException {
        db.update("UPDATE app_user SET failed_attempts=0, locked=FALSE WHERE id=?", id);
    }

    public void addPoints(long id, int delta) throws SQLException {
        db.update("UPDATE app_user SET points=points+? WHERE id=?", delta, id);
    }

    public void setMemberLevel(long id, int levelId) throws SQLException {
        db.update("UPDATE app_user SET member_level_id=? WHERE id=?", levelId, id);
    }

    public void unlock(long id) throws SQLException {
        db.update("UPDATE app_user SET failed_attempts=0, locked=FALSE WHERE id=?", id);
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.id = rs.getLong("id");
        u.username = rs.getString("username");
        u.passwordHash = rs.getString("password_hash");
        u.email = rs.getString("email");
        u.memberLevelId = rs.getInt("member_level_id");
        u.points = rs.getInt("points");
        u.failedAttempts = rs.getInt("failed_attempts");
        u.locked = rs.getBoolean("locked");
        Timestamp ts = rs.getTimestamp("created_at");
        u.createdAt = ts == null ? null : ts.toLocalDateTime();
        return u;
    }
}
