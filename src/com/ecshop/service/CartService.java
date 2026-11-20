package com.ecshop.service;

import com.ecshop.db.CartDao;
import com.ecshop.db.Database;
import com.ecshop.db.ProductDao;
import com.ecshop.model.CartItem;
import com.ecshop.model.Product;
import com.ecshop.util.BizException;

import java.sql.SQLException;
import java.util.List;

public class CartService {
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();

    public List<CartItem> list(long userId) {
        try {
            return cartDao.listByUser(userId);
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    /**
     * 加入购物车：已存在则累加数量。
     * 同时按"新增数量"预留（扣减）商品库存，库存不足时回滚报错。
     */
    public void add(long userId, long productId, int qty) {
        Database.getInstance().tx(() -> {
            if (qty <= 0) throw new BizException(400, "数量必须大于 0");
            Product p = getProduct(productId);
            try {
                CartItem existing = cartDao.findByUserProduct(userId, productId);
                reserveStock(productId, qty);
                if (existing == null) cartDao.insert(userId, productId, qty);
                else cartDao.updateQty(existing.id, existing.quantity + qty);
            } catch (SQLException e) {
                throw new BizException(500, "数据库错误: " + e.getMessage());
            }

            return null;
        });
    }

    /**
     * 设置购物车中某商品数量（<=0 则移除并释放库存）。
     * 数量增加的部分会扣减库存，减少的部分会回补库存。
     */
    public void update(long userId, long productId, int qty) {
        Database.getInstance().tx(() -> {
            if (qty <= 0) {
                remove(userId, productId);
            return null;
            }
            getProduct(productId);
            try {
                CartItem existing = cartDao.findByUserProduct(userId, productId);
                if (existing == null) {
                    add(userId, productId, qty);
                    return null;
                }
                int delta = qty - existing.quantity;
                if (delta > 0) reserveStock(productId, delta);
                else if (delta < 0) productDao.increaseStock(productId, -delta);
                cartDao.updateQty(existing.id, qty);
            } catch (SQLException e) {
                throw new BizException(500, "数据库错误: " + e.getMessage());
            }

            return null;
        });
    }

    /** 移除购物车商品：释放其预留的库存 */
    public void remove(long userId, long productId) {
        Database.getInstance().tx(() -> {
            try {
                CartItem existing = cartDao.findByUserProduct(userId, productId);
                if (existing != null) {
                    productDao.increaseStock(productId, existing.quantity);
                    cartDao.deleteItem(userId, productId);
                }
            } catch (SQLException e) {
                throw new BizException(500, "数据库错误: " + e.getMessage());
            }

            return null;
        });
    }

    /** 清空购物车：释放所有商品的预留库存 */
    public void clear(long userId) {
        Database.getInstance().tx(() -> {
            try {
                for (CartItem it : cartDao.listByUser(userId)) {
                    productDao.increaseStock(it.productId, it.quantity);
                }
                cartDao.deleteByUser(userId);
            } catch (SQLException e) {
                throw new BizException(500, "数据库错误: " + e.getMessage());
            }

            return null;
        });
    }

    /** 按新增数量预留库存（库存不足则报错） */
    private void reserveStock(long productId, int qty) {
        Product p = getProduct(productId);
        if (p.stock < qty) throw new BizException(400,
                "商品[" + p.name + "]库存不足（剩余 " + p.stock + "）");
        try {
            int affected = productDao.decreaseStock(productId, qty);
            if (affected == 0) throw new BizException(400, "商品[" + p.name + "]库存不足");
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }

    private Product getProduct(long id) {
        try {
            Product p = productDao.findById(id);
            if (p == null) throw new BizException(404, "商品不存在");
            return p;
        } catch (SQLException e) {
            throw new BizException(500, "数据库错误: " + e.getMessage());
        }
    }
}
