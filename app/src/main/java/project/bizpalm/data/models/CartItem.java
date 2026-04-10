package project.bizpalm.data.models;

import java.io.Serializable;
import project.bizpalm.data.entities.Product;

public class CartItem implements Serializable {
    public Product product;
    public int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return product.price * quantity;
    }
}
