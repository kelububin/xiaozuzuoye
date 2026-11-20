package com.ecshop.util;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

/**
 * 纯 JDK 实现的邮件发送服务（不依赖任何第三方 jar）。
 *
 * 支持两种常见 SMTP 方式：
 *   - 465 端口：隐式 SSL（连接即加密）
 *   - 587 端口：明文连接后 STARTTLS 升级为加密
 *
 * 配置从项目根目录的 mail.properties 读取，形如：
 *   mail.smtp.host=smtp.qq.com
 *   mail.smtp.port=465
 *   mail.user=你的邮箱@qq.com
 *   mail.password=邮箱授权码（不是登录密码）
 *   mail.from=你的邮箱@qq.com
 *
 * 未配置或 host 为空时，视为开发模式：验证码仅打印到控制台，不真正发送。
 */
public class EmailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;
    private final boolean configured;

    public EmailService() {
        Properties p = new Properties();
        boolean loaded = false;
        try (InputStream in = new FileInputStream("mail.properties")) {
            p.load(in);
            loaded = true;
        } catch (Exception ignored) {
            // 没有配置文件，走开发模式回退
        }
        this.host = p.getProperty("mail.smtp.host", "").trim();
        this.port = Integer.parseInt(p.getProperty("mail.smtp.port", "465").trim());
        this.username = p.getProperty("mail.user", "").trim();
        this.password = p.getProperty("mail.password", "").trim();
        this.from = p.getProperty("mail.from", username).trim();
        // 仍是模板占位符（"你的邮箱@qq.com" / "你的邮箱授权码"）时视为未配置，走开发模式
        boolean placeholder = username.contains("你的邮箱") || password.contains("你的邮箱授权码");
        this.configured = loaded && !host.isEmpty() && !username.isEmpty()
                && !password.isEmpty() && !placeholder;
    }

    /**
     * 发送登录验证码邮件。
     * 已配置真实 SMTP 时走真实发信；未配置或发送失败时回退为控制台打印验证码，
     * 保证登录流程不因邮件问题而中断，同时给出具体的排查提示。
     */
    public void sendCode(String toEmail, String code) {
        if (!configured) {
            devFallback(code, "未检测到 mail.properties 真实配置（可能仍是模板占位符）");
            return;
        }
        String message = buildMessage(toEmail, code);
        try {
            send(host, port, username, password, from, toEmail, message);
        } catch (Exception e) {
            String detail = e.getMessage() == null ? e.toString() : e.getMessage();
            devFallback(code, "验证码邮件真实发送失败：" + detail + diagnose(detail));
        }
    }

    /** 发送失败/未配置时的回退：验证码打印到控制台，登录流程继续 */
    private void devFallback(String code, String reason) {
        System.out.println("==================================================");
        System.out.println("  [提示] " + reason);
        System.out.println("  本次验证码回退为控制台显示：【" + code + "】");
        System.out.println("  如需真实邮箱接收验证码，请检查 mail.properties：");
        System.out.println("   1) mail.user / mail.from 改成你的真实完整邮箱");
        System.out.println("   2) mail.password 必须填【授权码】，不是邮箱登录密码");
        System.out.println("      QQ邮箱：网页版 设置 -> 账号 -> 开启 SMTP 服务 -> 生成授权码");
        System.out.println("   3) 若提示频率限制，等待几分钟后重试");
        System.out.println("==================================================");
    }

    /** 针对 QQ/163 常见 SMTP 错误码给出更具体的排查建议 */
    private String diagnose(String detail) {
        if (detail.contains("535")) {
            return " | 535=认证失败：通常是授权码错误、未开启 SMTP 服务或触发频率限制";
        }
        if (detail.contains("554")) {
            return " | 554=被拒信：通常因发件人与认证账号不一致或内容被判为垃圾邮件";
        }
        if (detail.contains("550")) {
            return " | 550=收件人不存在：请检查注册时填写的邮箱地址";
        }
        return "";
    }

    // ---------------- SMTP 发送核心（纯 Socket） ----------------

    private void send(String host, int port, String user, String pass, String from,
                      String to, String message) throws Exception {
        boolean useStarttls = (port == 587);
        Socket socket;
        if (!useStarttls) {
            // 465 隐式 SSL
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket(host, port);
        } else {
            // 587 先明文再 STARTTLS
            socket = new Socket(host, port);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        try {
            expect(reader, 220);
            sendLine(writer, "EHLO localhost");
            expect(reader, 250);

            if (useStarttls) {
                sendLine(writer, "STARTTLS");
                expect(reader, 220);
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket ssl = (SSLSocket) factory.createSocket(socket, host, port, true);
                socket = ssl;
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                sendLine(writer, "EHLO localhost");
                expect(reader, 250);
            }

            // 认证
            sendLine(writer, "AUTH LOGIN");
            expect(reader, 334);
            sendLine(writer, Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8)));
            expect(reader, 334);
            sendLine(writer, Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8)));
            expect(reader, 235);

            // 信封与内容
            sendLine(writer, "MAIL FROM:<" + from + ">");
            expect(reader, 250);
            sendLine(writer, "RCPT TO:<" + to + ">");
            expect(reader, 250);
            sendLine(writer, "DATA");
            expect(reader, 354);
            writer.write(message);
            writer.write("\r\n.\r\n");
            writer.flush();
            expect(reader, 250);

            sendLine(writer, "QUIT");
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private String buildMessage(String to, String code) {
        String subject = "=?UTF-8?B?"
                + Base64.getEncoder().encodeToString("【电商平台】您的登录验证码".getBytes(StandardCharsets.UTF_8))
                + "?=";
        String body = "您好，您正在登录电商平台控制台。\r\n"
                + "本次登录验证码为：" + code + "（5 分钟内有效）。\r\n"
                + "如非本人操作，请忽略本邮件并注意账号安全。";
        String body64 = Base64.getEncoder().encodeToString(body.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(from).append("\r\n");
        sb.append("To: ").append(to).append("\r\n");
        sb.append("Subject: ").append(subject).append("\r\n");
        sb.append("MIME-Version: 1.0\r\n");
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
        sb.append("Content-Transfer-Encoding: base64\r\n");
        sb.append("\r\n");
        sb.append(body64).append("\r\n");
        return sb.toString();
    }

    private void sendLine(BufferedWriter w, String line) throws Exception {
        w.write(line);
        w.write("\r\n");
        w.flush();
    }

    /** 读取 SMTP 响应，兼容多行（250-...）回复，并校验期望的状态码 */
    private void expect(BufferedReader r, int code) throws Exception {
        String line = r.readLine();
        while (line != null && line.length() >= 4 && line.startsWith(code + "-")) {
            line = r.readLine();
        }
        if (line == null || !line.startsWith(String.valueOf(code))) {
            throw new RuntimeException("SMTP 响应异常，期望 " + code + "，实际: " + line);
        }
    }
}
