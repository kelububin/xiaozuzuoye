package com.ecshop.service;

import com.ecshop.db.UserDao;
import com.ecshop.model.User;
import com.ecshop.util.BizException;
import com.ecshop.util.EmailService;
import com.ecshop.util.PasswordUtil;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDao userDao = new UserDao();
    private final EmailService emailService = new EmailService();
    private static final int MAX_ATTEMPTS = 3;          // 连续失败上限
    private static final int CODE_TTL_SECONDS = 5 * 60; // 验证码有效期 5 分钟
    private static final int CODE_LENGTH = 6;

    /** 邮箱验证码暂存（内存）：用户名 -> (验证码, 过期时间) */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    public User register(String username, String password, String email) {
        if (username == null || username.length() < 3) throw new BizException(400, "用户名至少 3 位");
        if (password == null || password.length() < 6) throw new BizException(400, "密码至少 6 位");
        if (email == null || email.isBlank() || !email.contains("@"))
            throw new BizException(400, "邮箱必填且需合法（登录需接收验证码）");
        try {
            if (userDao.findByUsername(username) != null) throw new BizException(409, "用户名已存在");
            String hash = PasswordUtil.hash(username, password);
            long id = userDao.insert(username, hash, email);
            return userDao.findById(id);
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    /**
     * 登录第一步：校验用户名与密码。
     * 密码正确后，向该账号绑定的邮箱发送登录验证码，并返回待验证的用户对象（尚未真正登录）。
     * 若账号未绑定邮箱或已被冻结，则直接拒绝。
     */
    public User prepareLogin(String username, String password) {
        try {
            User u = userDao.findByUsername(username);
            if (u == null) throw new BizException(401, "用户名或密码错误");
            if (u.locked) throw new BizException(423,
                    "账户已被冻结（连续登录失败超过 " + MAX_ATTEMPTS + " 次），请联系管理员解锁");
            if (u.email == null || u.email.isBlank())
                throw new BizException(400, "该账号未绑定邮箱，无法接收验证码，请联系管理员");

            if (!PasswordUtil.matches(username, password, u.passwordHash)) {
                int attempts = u.failedAttempts + 1;
                boolean lock = attempts >= MAX_ATTEMPTS;
                userDao.recordLoginFailure(u.id, attempts, lock);
                if (lock) throw new BizException(423, "账户已被冻结：连续登录失败超过 " + MAX_ATTEMPTS + " 次");
                throw new BizException(401, "用户名或密码错误（剩余尝试次数：" + (MAX_ATTEMPTS - attempts) + "）");
            }

            // 密码正确，生成并发送邮箱验证码
            String code = generateCode();
            codeStore.put(username, new CodeEntry(code, Instant.now().getEpochSecond() + CODE_TTL_SECONDS));
            emailService.sendCode(u.email, code);
            return u;
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    /**
     * 登录第二步：校验邮箱验证码。
     * 通过则清空失败计数、解冻（如有必要），返回登录成功的用户对象。
     */
    public User confirmLogin(String username, String code) {
        CodeEntry entry = codeStore.get(username);
        if (entry == null) throw new BizException(401, "请先输入用户名和密码获取验证码");
        if (Instant.now().getEpochSecond() > entry.expireAt) {
            codeStore.remove(username);
            throw new BizException(401, "验证码已过期，请重新登录获取");
        }
        if (code == null || !code.trim().equals(entry.code)) {
            throw new BizException(401, "验证码错误");
        }
        codeStore.remove(username);
        try {
            User u = userDao.findByUsername(username);
            userDao.resetLoginFailure(u.id); // 校验成功，清空失败计数并解冻
            return u;
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) sb.append((int) (Math.random() * 10));
        return sb.toString();
    }

    /** 验证码暂存结构 */
    private static final class CodeEntry {
        final String code;
        final long expireAt;
        CodeEntry(String code, long expireAt) { this.code = code; this.expireAt = expireAt; }
    }

    public User getProfile(long userId) {
        try {
            User u = userDao.findById(userId);
            if (u == null) throw new BizException(404, "用户不存在");
            return u;
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    public List<User> listUsers() {
        try {
            return userDao.findAll();
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    /** 管理员解冻账户（演示接口，生产应加权限校验） */
    public void unlock(long userId) {
        try {
            if (userDao.findById(userId) == null) throw new BizException(404, "用户不存在");
            userDao.unlock(userId);
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }
}
