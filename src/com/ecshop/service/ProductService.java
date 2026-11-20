package com.ecshop.service;

import com.ecshop.db.ProductDao;
import com.ecshop.model.Product;
import com.ecshop.util.BizException;

import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDao productDao = new ProductDao();

    public List<Product> list() {
        try {
            return productDao.findAll();
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    public Product get(long id) {
        try {
            Product p = productDao.findById(id);
            if (p == null) throw new BizException(404, "商品不存在");
            return p;
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }
}
