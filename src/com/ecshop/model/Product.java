package com.ecshop.model;

import java.time.LocalDateTime;

public class Product {
    public long id;
    public String name;
    public String description;
    public double price;
    public int stock;
    public String category;
    public LocalDateTime createdAt;
}
