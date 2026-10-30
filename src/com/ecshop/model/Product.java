package com.ecshop.model;

/** 商品实体。第一周先用少量示例数据练习对象与列表。 */
public class Product {
    public long id;
    public String name;
    public String description;
    public double price;
    public int stock;
    public String category;

    public Product(long id, String name, String description,
                   double price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
}

