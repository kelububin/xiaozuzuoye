package com.ecshop.console;

import com.ecshop.model.Product;
import com.ecshop.service.ProductService;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** 第一周基础菜单。后续逐步接入数据库与业务模块。 */
public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private final ProductService productService = new ProductService();

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
            for (Product product : productService.list()) {
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
            Product selected = productService.findById(id);
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

