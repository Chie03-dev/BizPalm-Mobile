package project.bizpalm.data.models;

import project.bizpalm.data.entities.Product;

public class RestockRecommendation {
    public Product product;
    public int daysLeft;
    public int suggestedRestock;

    public RestockRecommendation(Product product, int daysLeft, int suggestedRestock) {
        this.product = product;
        this.daysLeft = daysLeft;
        this.suggestedRestock = suggestedRestock;
    }
}
