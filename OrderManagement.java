import java.util.*;

/**
 * E-Commerce Order Processing System
 * 
 * Processes orders containing multiple products with:
 * - Subtotal calculation
 * - Category-specific discounts
 * - Coupon discounts
 * - GST calculation
 * - Shipping charges
 * - Final amount computation
 * 
 * Handles edge cases:
 * - Out-of-stock products
 * - Invalid coupon codes
 * - Maximum discount limits
 * - Free shipping thresholds
 * - Bulk-order discounts
 */
public class OrderManagement {

    // ========================== DATA MODELS ==========================

    /**
     * Represents a product in the catalog.
     */
    static class Product {
        String productId;
        String category;
        double unitPrice;
        int stockQuantity;

        public Product(String productId, String category, double unitPrice, int stockQuantity) {
            this.productId = productId;
            this.category = category;
            this.unitPrice = unitPrice;
            this.stockQuantity = stockQuantity;
        }
    }

    /**
     * Represents an item in the order (product + quantity + discount).
     */
    static class OrderItem {
        String productId;
        String category;
        int quantity;
        double unitPrice;
        double discountPercent; // item-level discount percentage
        double taxPercent;      // GST percentage for this item

        public OrderItem(String productId, String category, int quantity,
                         double unitPrice, double discountPercent, double taxPercent) {
            this.productId = productId;
            this.category = category;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercent = discountPercent;
            this.taxPercent = taxPercent;
        }
    }

    /**
     * Represents a coupon code with its discount details.
     */
    static class Coupon {
        String code;
        double discountPercent;
        double maxDiscount; // Maximum discount amount the coupon can provide

        public Coupon(String code, double discountPercent, double maxDiscount) {
            this.code = code;
            this.discountPercent = discountPercent;
            this.maxDiscount = maxDiscount;
        }
    }

    /**
     * Holds the complete billing breakdown for an order.
     */
    static class OrderBill {
        double subtotal;
        double categoryDiscount;
        double bulkDiscount;
        double couponDiscount;
        double totalDiscount;
        double taxableAmount;
        double gstAmount;
        double shippingCharge;
        double finalAmount;
        List<String> warnings;
        List<String> errors;

        public OrderBill() {
            this.warnings = new ArrayList<>();
            this.errors = new ArrayList<>();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("============= ORDER BILL =============\n");
            sb.append(String.format("Subtotal:             ₹%.2f%n", subtotal));
            sb.append(String.format("Category Discount:   -₹%.2f%n", categoryDiscount));
            sb.append(String.format("Bulk Discount:       -₹%.2f%n", bulkDiscount));
            sb.append(String.format("Coupon Discount:     -₹%.2f%n", couponDiscount));
            sb.append(String.format("Total Discount:      -₹%.2f%n", totalDiscount));
            sb.append(String.format("Taxable Amount:       ₹%.2f%n", taxableAmount));
            sb.append(String.format("GST:                 +₹%.2f%n", gstAmount));
            sb.append(String.format("Shipping:            +₹%.2f%n", shippingCharge));
            sb.append(String.format("Final Amount:         ₹%.2f%n", finalAmount));
            if (!warnings.isEmpty()) {
                sb.append("--- Warnings ---\n");
                for (String w : warnings) sb.append("  ⚠ ").append(w).append("\n");
            }
            if (!errors.isEmpty()) {
                sb.append("--- Errors ---\n");
                for (String e : errors) sb.append("  ✗ ").append(e).append("\n");
            }
            sb.append("======================================\n");
            return sb.toString();
        }
    }

    // ========================== CONSTANTS ==========================

    // Category-specific discount percentages
    static final Map<String, Double> CATEGORY_DISCOUNTS = new HashMap<>();
    static {
        CATEGORY_DISCOUNTS.put("Electronics", 5.0);
        CATEGORY_DISCOUNTS.put("Clothing", 10.0);
        CATEGORY_DISCOUNTS.put("Groceries", 2.0);
        CATEGORY_DISCOUNTS.put("Books", 15.0);
        CATEGORY_DISCOUNTS.put("Furniture", 8.0);
        CATEGORY_DISCOUNTS.put("Sports", 7.0);
    }

    // Valid coupons
    static final Map<String, Coupon> VALID_COUPONS = new HashMap<>();
    static {
        VALID_COUPONS.put("SAVE10", new Coupon("SAVE10", 10.0, 500.0));
        VALID_COUPONS.put("FLAT20", new Coupon("FLAT20", 20.0, 1000.0));
        VALID_COUPONS.put("MEGA50", new Coupon("MEGA50", 50.0, 2000.0));
        VALID_COUPONS.put("NEW5", new Coupon("NEW5", 5.0, 200.0));
    }

    // Product catalog with stock
    static final Map<String, Product> PRODUCT_CATALOG = new HashMap<>();
    static {
        PRODUCT_CATALOG.put("P001", new Product("P001", "Electronics", 15000.0, 10));
        PRODUCT_CATALOG.put("P002", new Product("P002", "Clothing", 2500.0, 50));
        PRODUCT_CATALOG.put("P003", new Product("P003", "Groceries", 500.0, 200));
        PRODUCT_CATALOG.put("P004", new Product("P004", "Books", 800.0, 100));
        PRODUCT_CATALOG.put("P005", new Product("P005", "Furniture", 25000.0, 5));
        PRODUCT_CATALOG.put("P006", new Product("P006", "Electronics", 45000.0, 3));
        PRODUCT_CATALOG.put("P007", new Product("P007", "Sports", 3500.0, 30));
        PRODUCT_CATALOG.put("P008", new Product("P008", "Clothing", 1200.0, 0)); // Out of stock
        PRODUCT_CATALOG.put("P009", new Product("P009", "Groceries", 150.0, 500));
        PRODUCT_CATALOG.put("P010", new Product("P010", "Books", 350.0, 75));
    }

    static final double FREE_SHIPPING_THRESHOLD = 2000.0;
    static final double SHIPPING_CHARGE = 150.0;
    static final double BULK_ORDER_THRESHOLD = 10; // quantity threshold for bulk discount
    static final double BULK_DISCOUNT_PERCENT = 5.0;
    static final double MAX_TOTAL_DISCOUNT_PERCENT = 40.0; // max discount cap on subtotal

    // ========================== CORE LOGIC ==========================

    /**
     * Processes an order and returns the bill.
     *
     * @param items      List of order items
     * @param couponCode Coupon code (can be null or empty)
     * @return OrderBill with full breakdown
     */
    public static OrderBill processOrder(List<OrderItem> items, String couponCode) {
        OrderBill bill = new OrderBill();

        if (items == null || items.isEmpty()) {
            bill.errors.add("Order contains no items.");
            return bill;
        }

        List<OrderItem> validItems = new ArrayList<>();

        // ---------- Validate each item ----------
        for (OrderItem item : items) {
            // Check for invalid product ID
            if (!PRODUCT_CATALOG.containsKey(item.productId)) {
                bill.errors.add("Invalid product ID: " + item.productId);
                continue;
            }

            Product catalogProduct = PRODUCT_CATALOG.get(item.productId);

            // Check for zero quantity
            if (item.quantity == 0) {
                bill.warnings.add("Zero quantity for product " + item.productId + " — skipped.");
                continue;
            }

            // Check for negative quantity
            if (item.quantity < 0) {
                bill.errors.add("Negative quantity (" + item.quantity + ") for product " + item.productId);
                continue;
            }

            // Check stock
            if (catalogProduct.stockQuantity == 0) {
                bill.errors.add("Product " + item.productId + " is out of stock.");
                continue;
            }
            if (item.quantity > catalogProduct.stockQuantity) {
                bill.warnings.add("Only " + catalogProduct.stockQuantity + " units available for "
                        + item.productId + ". Adjusted from " + item.quantity + ".");
                item.quantity = catalogProduct.stockQuantity;
            }

            validItems.add(item);
        }

        if (validItems.isEmpty()) {
            bill.errors.add("No valid items to process in the order.");
            return bill;
        }

        // ---------- Calculate subtotal ----------
        double subtotal = 0;
        for (OrderItem item : validItems) {
            subtotal += item.unitPrice * item.quantity;
        }
        bill.subtotal = Math.round(subtotal * 100.0) / 100.0;

        // ---------- Category-specific discount ----------
        double categoryDiscount = 0;
        for (OrderItem item : validItems) {
            double catDiscPercent = CATEGORY_DISCOUNTS.getOrDefault(item.category, 0.0);
            categoryDiscount += (item.unitPrice * item.quantity) * (catDiscPercent / 100.0);
        }
        bill.categoryDiscount = Math.round(categoryDiscount * 100.0) / 100.0;

        // ---------- Bulk-order discount ----------
        double bulkDiscount = 0;
        int totalQuantity = 0;
        for (OrderItem item : validItems) {
            totalQuantity += item.quantity;
        }
        if (totalQuantity >= BULK_ORDER_THRESHOLD) {
            bulkDiscount = subtotal * (BULK_DISCOUNT_PERCENT / 100.0);
        }
        bill.bulkDiscount = Math.round(bulkDiscount * 100.0) / 100.0;

        // ---------- Item-level discount ----------
        double itemDiscount = 0;
        for (OrderItem item : validItems) {
            itemDiscount += (item.unitPrice * item.quantity) * (item.discountPercent / 100.0);
        }

        // ---------- Coupon discount ----------
        double couponDiscount = 0;
        if (couponCode != null && !couponCode.isEmpty()) {
            Coupon coupon = VALID_COUPONS.get(couponCode.toUpperCase());
            if (coupon == null) {
                bill.errors.add("Invalid coupon code: " + couponCode);
            } else {
                couponDiscount = subtotal * (coupon.discountPercent / 100.0);
                if (couponDiscount > coupon.maxDiscount) {
                    couponDiscount = coupon.maxDiscount;
                    bill.warnings.add("Coupon discount capped at ₹" + coupon.maxDiscount);
                }
            }
        }
        bill.couponDiscount = Math.round(couponDiscount * 100.0) / 100.0;

        // ---------- Total discount with cap ----------
        double totalDiscount = categoryDiscount + bulkDiscount + itemDiscount + couponDiscount;
        double maxAllowedDiscount = subtotal * (MAX_TOTAL_DISCOUNT_PERCENT / 100.0);
        if (totalDiscount > maxAllowedDiscount) {
            bill.warnings.add("Total discount capped at " + MAX_TOTAL_DISCOUNT_PERCENT
                    + "% of subtotal (₹" + String.format("%.2f", maxAllowedDiscount) + ").");
            totalDiscount = maxAllowedDiscount;
        }
        bill.totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        // ---------- Taxable amount ----------
        double taxableAmount = subtotal - totalDiscount;
        if (taxableAmount < 0) taxableAmount = 0;
        bill.taxableAmount = Math.round(taxableAmount * 100.0) / 100.0;

        // ---------- GST ----------
        double gstAmount = 0;
        // Apply weighted GST based on each item's tax rate
        for (OrderItem item : validItems) {
            double itemTotal = item.unitPrice * item.quantity;
            double itemShare = (subtotal > 0) ? (itemTotal / subtotal) : 0;
            double itemTaxable = taxableAmount * itemShare;
            gstAmount += itemTaxable * (item.taxPercent / 100.0);
        }
        bill.gstAmount = Math.round(gstAmount * 100.0) / 100.0;

        // ---------- Shipping ----------
        double shippingCharge = SHIPPING_CHARGE;
        if (taxableAmount >= FREE_SHIPPING_THRESHOLD) {
            shippingCharge = 0;
            bill.warnings.add("Free shipping applied (order ≥ ₹" + FREE_SHIPPING_THRESHOLD + ").");
        }
        bill.shippingCharge = shippingCharge;

        // ---------- Final amount ----------
        double finalAmount = taxableAmount + gstAmount + shippingCharge;
        bill.finalAmount = Math.round(finalAmount * 100.0) / 100.0;

        return bill;
    }

    // ========================== MAIN ==========================

    public static void main(String[] args) {
        System.out.println("=== E-Commerce Order Processing System ===\n");

        // Sample order with multiple products
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", "Electronics", 2, 15000.0, 0, 18.0));
        items.add(new OrderItem("P002", "Clothing", 3, 2500.0, 5, 12.0));
        items.add(new OrderItem("P004", "Books", 5, 800.0, 0, 5.0));
        items.add(new OrderItem("P009", "Groceries", 4, 150.0, 0, 5.0));

        OrderBill bill = processOrder(items, "SAVE10");
        System.out.println(bill);

        // Order with out-of-stock product
        System.out.println("--- Order with out-of-stock item ---");
        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem("P008", "Clothing", 2, 1200.0, 0, 12.0));
        items2.add(new OrderItem("P003", "Groceries", 1, 500.0, 0, 5.0));

        OrderBill bill2 = processOrder(items2, null);
        System.out.println(bill2);

        // Order with invalid coupon
        System.out.println("--- Order with invalid coupon ---");
        List<OrderItem> items3 = new ArrayList<>();
        items3.add(new OrderItem("P006", "Electronics", 1, 45000.0, 0, 18.0));

        OrderBill bill3 = processOrder(items3, "FAKECODE");
        System.out.println(bill3);
    }
}
