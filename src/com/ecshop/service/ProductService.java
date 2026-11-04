package com.ecshop.service;

import com.ecshop.model.Product;
import java.util.List;

/** 为控制台提供示例商品列表和按编号查询。 */
public class ProductService {
    private final List<Product> products = List.of(
            new Product(1, "无线蓝牙耳机", "主动降噪，续航30小时", 299.00, 100, "数码"),
            new Product(2, "机械键盘", "RGB背光，红轴", 459.00, 60, "数码"),
            new Product(3, "精品咖啡豆", "中度烘焙 500g", 89.00, 200, "食品"),
            new Product(4, "运动水杯", "大容量 1L 食品级", 49.00, 300, "生活")
    );

    public List<Product> list() {
        return products;
    }

    public Product findById(long id) {
        for (Product product : products) {
            if (product.id == id) {
                return product;
            }
        }
        return null;
    }
}
