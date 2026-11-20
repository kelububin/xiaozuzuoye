package com.ecshop.model;

import java.time.LocalDateTime;

public class User {
    public long id;
    public String username;
    public String passwordHash;
    public String email;
    public int memberLevelId;
    public int points;
    public int failedAttempts;   // 连续登录失败次数
    public boolean locked;       // 是否冻结
    public LocalDateTime createdAt;
}
