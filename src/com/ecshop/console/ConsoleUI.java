package com.ecshop.console;

import com.ecshop.model.CartItem;
import com.ecshop.model.Product;
import com.ecshop.model.User;
import com.ecshop.service.CartService;
import com.ecshop.service.ProductService;
import com.ecshop.service.UserService;
import com.ecshop.util.BizException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private final UserService users = new UserService();
    private final ProductService products = new ProductService();
    private final CartService cart = new CartService();
    private User currentUser;

    public void start() {
        System.out.println("电商平台控制台系统 第4周第2次课");
        while (true) {
            System.out.println(currentUser == null
                    ? "\n1.注册  2.登录  3.浏览商品  0.退出"
                    : "\n1.浏览商品  2.购物车  3.退出登录  0.退出");
            String choice = read("请选择: ");
            if (choice == null || choice.equals("0")) return;
            try {
                if (currentUser == null) {
                    switch (choice) {
                        case "1" -> register();
                        case "2" -> login();
                        case "3" -> browse();
                        default -> System.out.println("无效选项");
                    }
                } else {
                    switch (choice) {
                        case "1" -> browse();
                        case "2" -> cartMenu();
                        case "3" -> { currentUser = null; System.out.println("已退出登录"); }
                        default -> System.out.println("无效选项");
                    }
                }
            } catch (BizException | IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
            }
        }
    }

    private void register() {
        String name = required("用户名: ");
        String password = required("密码: ");
        String email = required("邮箱: ");
        users.register(name, password, email);
        System.out.println("注册成功");
    }

    private void login() {
        String name = required("用户名: ");
        String password = required("密码: ");
        users.prepareLogin(name, password);
        currentUser = users.confirmLogin(name, required("请输入窗口显示的验证码: "));
        System.out.println("登录成功，欢迎 " + currentUser.username);
    }

    private void browse() {
        while (true) {
            for (Product product : products.list()) {
                System.out.printf("[%d] %s %.2f元 库存=%d%n", product.id, product.name, product.price, product.stock);
            }
            String value = read("输入商品ID查看详情，0返回: ");
            if (value == null || value.equals("0")) return;
            try {
                Product product = products.get(Long.parseLong(value));
                System.out.printf("%s：%s 分类=%s%n", product.name, product.description, product.category);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的商品编号");
            } catch (BizException e) {
                System.out.println("错误: " + e.getMessage());
            }
        }
    }

    private void cartMenu() {
        while (true) {
            System.out.println("\n1.查看  2.添加  3.修改数量  4.移除  5.清空  0.返回");
            String choice = read("选择: ");
            if (choice == null || choice.equals("0")) return;
            try {
                switch (choice) {
                    case "1" -> {
                        var items = cart.list(currentUser.id);
                        if (items.isEmpty()) System.out.println("购物车为空");
                        for (CartItem item : items) {
                            System.out.printf("[%d] %s x%d 单价 %.2f 小计 %.2f%n", item.productId, item.productName, item.quantity, item.unitPrice, item.subtotal);
                        }
                    }
                    case "2" -> { long id = number("商品ID: "); int qty = quantity("数量: "); cart.add(currentUser.id, id, qty); System.out.println("已加入"); }
                    case "3" -> { long id = number("商品ID: "); int qty = quantity("新数量（不大于0时移除）: "); cart.update(currentUser.id, id, qty); System.out.println("已更新"); }
                    case "4" -> { cart.remove(currentUser.id, number("商品ID: ")); System.out.println("已移除"); }
                    case "5" -> { cart.clear(currentUser.id); System.out.println("已清空"); }
                    default -> System.out.println("无效选项");
                }
            } catch (BizException | IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
            }
        }
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    private String required(String prompt) {
        String value = read(prompt);
        if (value == null) throw new IllegalArgumentException("输入已结束");
        return value;
    }

    private long number(String prompt) {
        try { return Long.parseLong(required(prompt)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("请输入整数"); }
    }

    private int quantity(String prompt) {
        long value = number(prompt);
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) throw new IllegalArgumentException("数量超出范围");
        return (int) value;
    }
}
