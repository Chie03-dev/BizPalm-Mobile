package project.bizpalm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class GenerateTestData {

    private static class ProductData {
        int id;
        String name;
        String barcode;
        double retail;
        double cost;
        int qty;
        String status;
        String popular;
        String expiry;

        ProductData(int id, String name, String barcode, double retail, double cost, int qty, String status, String popular, String expiry) {
            this.id = id;
            this.name = name;
            this.barcode = barcode;
            this.retail = retail;
            this.cost = cost;
            this.qty = qty;
            this.status = status;
            this.popular = popular;
            this.expiry = expiry;
        }
    }

    @Test
    public void generateExcelFile() {
        Workbook workbook = new XSSFWorkbook();
        Random random = new Random();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 1. Define 20 Products
        List<ProductData> products = new ArrayList<>();
        products.add(new ProductData(1, "Premium Coffee", "1001", 250.0, 150.0, 50, "In Stock", "Yes", "2026-12-01"));
        products.add(new ProductData(2, "Whole Wheat Bread", "1002", 65.0, 45.0, 12, "In Stock", "No", "2026-05-15"));
        products.add(new ProductData(3, "Fresh Milk 1L", "1003", 95.0, 70.0, 5, "Low Stock", "Yes", "2026-04-10"));
        products.add(new ProductData(4, "Organic Eggs (12s)", "1004", 180.0, 140.0, 20, "In Stock", "Yes", "2026-04-20"));
        products.add(new ProductData(5, "Bottled Water 500ml", "1005", 20.0, 8.0, 100, "In Stock", "No", "2027-01-01"));
        products.add(new ProductData(6, "Dark Chocolate", "1006", 120.0, 50.0, 15, "In Stock", "No", "2026-06-30"));
        products.add(new ProductData(7, "Cooking Oil 1L", "1007", 110.0, 95.0, 8, "Low Stock", "No", "2026-05-20"));
        products.add(new ProductData(8, "Jasmine Rice 5kg", "1008", 350.0, 280.0, 30, "In Stock", "Yes", "2026-10-10"));
        products.add(new ProductData(9, "Instant Noodles", "1009", 15.0, 12.0, 200, "In Stock", "No", "2026-12-31"));
        products.add(new ProductData(10, "Liquid Detergent", "1010", 185.0, 130.0, 25, "In Stock", "No", "2027-03-15"));
        products.add(new ProductData(11, "Shampoo 200ml", "1011", 150.0, 110.0, 40, "In Stock", "No", "2027-06-10"));
        products.add(new ProductData(12, "Soap Bar", "1012", 35.0, 22.0, 60, "In Stock", "No", "2027-08-20"));
        products.add(new ProductData(13, "Toothpaste", "1013", 85.0, 60.0, 35, "In Stock", "No", "2027-01-15"));
        products.add(new ProductData(14, "Dishwashing Liquid", "1014", 45.0, 30.0, 45, "In Stock", "No", "2026-11-05"));
        products.add(new ProductData(15, "Canned Tuna", "1015", 55.0, 40.0, 80, "In Stock", "No", "2028-02-28"));
        products.add(new ProductData(16, "Soda 1.5L", "1016", 75.0, 55.0, 50, "In Stock", "Yes", "2026-09-12"));
        products.add(new ProductData(17, "Potato Chips", "1017", 45.0, 25.0, 30, "In Stock", "No", "2026-07-22"));
        products.add(new ProductData(18, "Biscuits Pack", "1018", 25.0, 15.0, 100, "In Stock", "No", "2026-12-15"));
        products.add(new ProductData(19, "Laundry Powder 1kg", "1019", 120.0, 85.0, 20, "In Stock", "No", "2027-05-30"));
        products.add(new ProductData(20, "Toilet Paper 4pk", "1020", 60.0, 40.0, 40, "In Stock", "No", "2028-10-01"));

        // --- Sheet 1: Inventory Products ---
        Sheet productSheet = workbook.createSheet("Inventory Products");
        String[] productHeaders = {"ID", "Product Name", "Barcode", "Retail Price (₱)", "Cost Price (₱)", "Quantity", "Status", "Popular", "Expiry Date"};
        Row prodHeaderRow = productSheet.createRow(0);
        for (int i = 0; i < productHeaders.length; i++) prodHeaderRow.createCell(i).setCellValue(productHeaders[i]);

        for (int i = 0; i < products.size(); i++) {
            ProductData p = products.get(i);
            Row row = productSheet.createRow(i + 1);
            row.createCell(0).setCellValue(p.id);
            row.createCell(1).setCellValue(p.name);
            row.createCell(2).setCellValue(p.barcode);
            row.createCell(3).setCellValue(p.retail);
            row.createCell(4).setCellValue(p.cost);
            row.createCell(5).setCellValue(p.qty);
            row.createCell(6).setCellValue(p.status);
            row.createCell(7).setCellValue(p.popular);
            row.createCell(8).setCellValue(p.expiry);
        }

        // 2. Generate 50 Transactions and Items
        Sheet transSheet = workbook.createSheet("Sales Transactions");
        String[] transHeaders = {"Transaction ID", "Total Amount (₱)", "Cash Received", "Change", "Date & Time", "Payment Type"};
        Row transHeaderRow = transSheet.createRow(0);
        for (int i = 0; i < transHeaders.length; i++) transHeaderRow.createCell(i).setCellValue(transHeaders[i]);

        Sheet itemsSheet = workbook.createSheet("Transaction Items");
        String[] itemHeaders = {"Transaction ID", "Product Barcode", "Quantity Sold", "Amount Due (₱)", "Unit Cost (₱)"};
        Row itemHeaderRow = itemsSheet.createRow(0);
        for (int i = 0; i < itemHeaders.length; i++) itemHeaderRow.createCell(i).setCellValue(itemHeaders[i]);

        int itemRowIdx = 1;
        Calendar cal = Calendar.getInstance();
        long startTime = 1704067200000L; // 2024-01-01
        long endTime = 1773964800000L;   // 2026-03-20
        long diff = endTime - startTime;

        for (int tId = 1; tId <= 50; tId++) {
            long randomTime = startTime + (long) (random.nextDouble() * diff);
            String dateStr = sdf.format(new java.util.Date(randomTime));
            
            int numItems = random.nextInt(4) + 1; // 1 to 4 items per transaction
            double totalAmount = 0;
            
            for (int j = 0; j < numItems; j++) {
                ProductData p = products.get(random.nextInt(products.size()));
                int qtySold = random.nextInt(3) + 1;
                double itemTotal = p.retail * qtySold;
                totalAmount += itemTotal;

                Row row = itemsSheet.createRow(itemRowIdx++);
                row.createCell(0).setCellValue(tId);
                row.createCell(1).setCellValue(p.barcode);
                row.createCell(2).setCellValue(qtySold);
                row.createCell(3).setCellValue(itemTotal);
                row.createCell(4).setCellValue(p.cost);
            }

            double cashReceived = Math.ceil(totalAmount / 50.0) * 50.0;
            if (random.nextBoolean()) cashReceived = totalAmount; // Exact payment
            
            Row row = transSheet.createRow(tId);
            row.createCell(0).setCellValue(tId);
            row.createCell(1).setCellValue(totalAmount);
            row.createCell(2).setCellValue(cashReceived);
            row.createCell(3).setCellValue(cashReceived - totalAmount);
            row.createCell(4).setCellValue(dateStr);
            row.createCell(5).setCellValue(random.nextBoolean() ? "Cash" : "Online");
        }

        try (FileOutputStream fileOut = new FileOutputStream("D:/BizPalm_TestData.xlsx")) {
            workbook.write(fileOut);
            System.out.println("Excel file generated at: D:/BizPalm_TestData.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { workbook.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
