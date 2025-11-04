package org.example.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.annotation.Generatable;

@Generatable
public class CartStorage {
    private final Map<String, List<Cart>> buyerCarts;

    public CartStorage(Map<String, List<Cart>> buyerCarts) {
        this.buyerCarts = buyerCarts;
    }

    public Map<String, List<Cart>> getBuyerCarts() {
        return buyerCarts;
    }

    public void addBuyerCart(String buyer, Cart cart) {
        buyerCarts.computeIfAbsent(buyer, k -> new ArrayList<>()).add(cart);
    }

    @Override
    public String toString() {
        return "CartStorage{" +
                "buyerCarts=" + buyerCarts +
                '}';
    }
}
