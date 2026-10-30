package com.ecshop.console;

import com.ecshop.model.Product;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/** 第一周基础菜单。后续逐步接入数据库与业务模块。 */
public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private final List<Product> products = List.of(
            new Product(1, "无线蓝牙耳机", "主动降噪，续航30小时", 299.00, 100, "数码"),
            new Product(2, "机械键盘", "RGB背光，红轴", 459.00, 60, "数码"),
            new Product(3, "精品咖啡豆", "中度烘焙 500g", 89.00, 200, "食品"),
            new Product(4, "运动水杯", "大容量 1L 食品级", 49.00, 300, "生活")
    );

    public void start() {
        System.out.println("电商平台控制台系统 第一周原型");
        while (true) {
            System.out.println("\n===== 主菜单 =====");
            System.out.println("1. 浏览商品");
            System.out.println("2. 项目介绍");
            System.out.println("0. 退出系统");
            String choice = readLine("请选择: ");
            if (choice == null || choice.equals("0")) {
                System.out.println("再见。");
                return;
            }
            switch (choice) {
                case "1" -> browseProducts();
                case "2" -> showIntroduction();
                default -> System.out.println("无效选项，请输入0、1或2。");
            }
        }
    }

    private void browseProducts() {
        while (true) {
            System.out.println("\n---- 示例商品列表 ----");
            for (Product product : products) {
                System.out.printf("[%d] %s  %.2f元  库存=%d%n",
                        product.id, product.name, product.price, product.stock);
            }
            String input = readLine("输入商品ID查看详情，0返回: ");
            if (input == null || input.equals("0")) return;
            long id;
            try {
                id = Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的商品编号。");
                continue;
            }
            Product selected = null;
            for (Product product : products) {
                if (product.id == id) {
                    selected = product;
                    break;
                }
            }
            if (selected == null) {
                System.out.println("商品不存在，请重新选择。");
            } else {
                System.out.printf("商品：%s%n价格：%.2f元%n库存：%d%n分类：%s%n描述：%s%n",
                        selected.name, selected.price, selected.stock,
                        selected.category, selected.description);
            }
        }
    }

    private void showIntroduction() {
        System.out.println("本项目使用Java控制台演示电商购物流程。");
        System.out.println("第一周已完成需求梳理、分工和商品浏览基础原型。");
        System.out.println("后续计划使用JDBC与MySQL，逐步开发账户、购物车和订单等功能。");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }
}

