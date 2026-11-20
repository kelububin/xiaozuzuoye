package com.ecshop.db;

import com.ecshop.model.CartItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CartDao {
    private final Database db = Database.getInstance();

    public List<CartItem> listByUser(long userId) throws SQLException {
        return db.query(
            "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, " +
            "p.price AS unit_price, (p.price * c.quantity) AS subtotal " +
            "FROM cart_item c JOIN product p ON c.product_id=p.id " +
            "WHERE c.user_id=? ORDER BY c.id", this::map, userId);
    }

    public CartItem findByUserProduct(long userId, long productId) throws SQLException {
        return db.queryOne(
            "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, " +
            "p.price AS unit_price, (p.price * c.quantity) AS subtotal " +
            "FROM cart_item c JOIN product p ON c.product_id=p.id " +
            "WHERE c.user_id=? AND c.product_id=?", this::map, userId, productId);
    }

    public long insert(long userId, long productId, int qty) throws SQLException {
        return db.insertAndGetKey(
            "INSERT INTO cart_item(user_id,product_id,quantity) VALUES(?,?,?)",
            userId, productId, qty);
    }

    public void updateQty(long id, int qty) throws SQLException {
        db.update("UPDATE cart_item SET quantity=? WHERE id=?", qty, id);
    }

    public void delete(long id) throws SQLException {
        db.update("DELETE FROM cart_item WHERE id=?", id);
    }

    public void deleteByUser(long userId) throws SQLException {
        db.update("DELETE FROM cart_item WHERE user_id=?", userId);
    }

    public void deleteItem(long userId, long productId) throws SQLException {
        db.update("DELETE FROM cart_item WHERE user_id=? AND product_id=?", userId, productId);
    }

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem c = new CartItem();
        c.id = rs.getLong("id");
        c.userId = rs.getLong("user_id");
        c.productId = rs.getLong("product_id");
        c.quantity = rs.getInt("quantity");
        c.productName = rs.getString("product_name");
        c.unitPrice = rs.getDouble("unit_price");
        c.subtotal = rs.getDouble("subtotal");
        return c;
    }
}
