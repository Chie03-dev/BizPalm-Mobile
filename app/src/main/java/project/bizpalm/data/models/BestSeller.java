package project.bizpalm.data.models;

public class BestSeller {
    public String productName;
    public int totalQuantity;
    public int productId;

    public BestSeller(String productName, int totalQuantity, int productId) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.productId = productId;
    }
}
