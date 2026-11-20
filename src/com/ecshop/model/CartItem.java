package com.ecshop.model;

public class CartItem {
    public long id;
    public long userId;
    public long productId;
    public int quantity;
    // 关联查询字段（用于购物车视图）
    public String productName;
    public double unitPrice;
    public double subtotal;
}
