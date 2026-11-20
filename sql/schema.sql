-- 第02周第01次课：基础表结构与演示数据
CREATE DATABASE IF NOT EXISTS `xiaozuzuoye_week02`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `xiaozuzuoye_week02`;

-- 1. 会员等级表
CREATE TABLE IF NOT EXISTS member_level (
    id              INT PRIMARY KEY,
    name            VARCHAR(20)  NOT NULL,
    threshold       INT          NOT NULL,   -- 升级所需累计积分门槛
    discount_rate   DECIMAL(4,3) NOT NULL,   -- 会员商品折扣 (1.0=无折扣, 0.90=9折)
    points_per_yuan DECIMAL(4,2) NOT NULL    -- 每消费 1 元获得的积分
);

-- 2. 用户表（客户，无手机号字段）
CREATE TABLE IF NOT EXISTS app_user (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    email            VARCHAR(100),
    member_level_id  INT          NOT NULL DEFAULT 1,
    points           INT          NOT NULL DEFAULT 0,
    failed_attempts  INT          NOT NULL DEFAULT 0,   -- 连续登录失败次数
    locked           BOOLEAN      NOT NULL DEFAULT FALSE, -- 是否冻结
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_level_id) REFERENCES member_level(id)
);

-- 3. 商品表
CREATE TABLE IF NOT EXISTS product (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
    price        DECIMAL(12,2) NOT NULL,
    stock        INT           NOT NULL DEFAULT 0,
    category     VARCHAR(50),
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 示例会员等级
INSERT INTO member_level (id, name, threshold, discount_rate, points_per_yuan)
SELECT 1, '普通会员', 0,    1.000, 1.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member_level WHERE id = 1);
INSERT INTO member_level (id, name, threshold, discount_rate, points_per_yuan)
SELECT 2, '银卡会员', 1000, 0.950, 1.20 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member_level WHERE id = 2);
INSERT INTO member_level (id, name, threshold, discount_rate, points_per_yuan)
SELECT 3, '金卡会员', 5000, 0.900, 1.50 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member_level WHERE id = 3);
INSERT INTO member_level (id, name, threshold, discount_rate, points_per_yuan)
SELECT 4, '钻石会员', 20000, 0.850, 2.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member_level WHERE id = 4);

-- 示例商品
INSERT INTO product (name, description, price, stock, category)
SELECT '无线蓝牙耳机', '主动降噪，续航30小时', 299.00, 100, '数码' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '无线蓝牙耳机');
INSERT INTO product (name, description, price, stock, category)
SELECT '机械键盘', 'RGB背光，红轴', 459.00, 60, '数码' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '机械键盘');
INSERT INTO product (name, description, price, stock, category)
SELECT '精品咖啡豆', '中度烘焙 500g', 89.00, 200, '食品' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '精品咖啡豆');
INSERT INTO product (name, description, price, stock, category)
SELECT '运动水杯', '大容量 1L 食品级', 49.00, 300, '生活' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '运动水杯');

-- 4. 购物车表
CREATE TABLE IF NOT EXISTS cart_item (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, product_id),
    FOREIGN KEY (user_id)    REFERENCES app_user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);
