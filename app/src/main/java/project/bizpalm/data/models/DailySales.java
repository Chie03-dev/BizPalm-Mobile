package project.bizpalm.data.models;

public class DailySales {
    public String date;
    public double totalAmount; // Revenue
    public double totalProfit; // Gross Profit

    public DailySales(String date, double totalAmount, double totalProfit) {
        this.date = date;
        this.totalAmount = totalAmount;
        this.totalProfit = totalProfit;
    }
}
