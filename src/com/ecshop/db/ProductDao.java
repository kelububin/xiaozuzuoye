package com.ecshop.db;

import com.ecshop.model.Product;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ProductDao {
    private final Database db = Database.getInstance();

    public List<Product> findAll() throws SQLException {
        return db.query(
            "SELECT id,name,description,price,stock,category,created_at FROM product ORDER BY id",
            this::map);
    }

    public Product findById(long id) throws SQLException {
        return db.queryOne(
            "SELECT id,name,description,price,stock,category,created_at FROM product WHERE id=?",
            this::map, id);
    }

    /** 扣减库存，返回受影响行数（0 表示库存不足） */
    public int decreaseStock(long id, int qty) throws SQLException {
        return db.update(
            "UPDATE product SET stock=stock-? WHERE id=? AND stock>=?", qty, id, qty);
    }

    /** 回补库存（购物车移除 / 订单取消退款时释放预留） */
    public void increaseStock(long id, int qty) throws SQLException {
        db.update("UPDATE product SET stock=stock+? WHERE id=?", qty, id);
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.id = rs.getLong("id");
        p.name = rs.getString("name");
        p.description = rs.getString("description");
        p.price = rs.getDouble("price");
        p.stock = rs.getInt("stock");
        p.category = rs.getString("category");
        Timestamp ts = rs.getTimestamp("created_at");
        p.createdAt = ts == null ? null : ts.toLocalDateTime();
        return p;
    }
}
