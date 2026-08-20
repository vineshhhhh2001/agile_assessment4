import java.util.*;

/**
 * QA Test Suite for E-Commerce Order Processing System
 * Tests 25+ combinations covering all edge cases.
 */
public class OrderManagementQA {

    static int testsPassed = 0;
    static int testsFailed = 0;

    static void assertEqual(double expected, double actual, String testName) {
        if (Math.abs(expected - actual) < 0.01) {
            testsPassed++;
            System.out.println("  ✓ PASS: " + testName + " (Expected: " + expected + ", Got: " + actual + ")");
        } else {
            testsFailed++;
            System.out.println("  ✗ FAIL: " + testName + " (Expected: " + expected + ", Got: " + actual + ")");
        }
    }

    static void assertTrue(boolean condition, String testName) {
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ PASS: " + testName);
        } else {
            testsFailed++;
            System.out.println("  ✗ FAIL: " + testName);
        }
    }

    // ==================== TEST CASES ====================

    // Test 1: Single product order
    static void testSingleProduct() {
        System.out.println("\nTest 1: Single Product Order");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 1, 15000.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(15000.0, bill.subtotal, "Subtotal");
        assertEqual(750.0, bill.categoryDiscount, "Category discount (Electronics 5%)");
        assertTrue(bill.errors.isEmpty(), "No errors");
    }

    // Test 2: Multiple products order
    static void testMultipleProducts() {
        System.out.println("\nTest 2: Multiple Products Order");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 1, 15000.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("P002", "Clothing", 2, 2500.0, 0, 12.0));
        items.add(new OrderManagement.OrderItem("P004", "Books", 1, 800.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(20800.0, bill.subtotal, "Subtotal");
        assertTrue(bill.finalAmount > 0, "Final amount is positive");
    }

    // Test 3: Zero quantity product
    static void testZeroQuantity() {
        System.out.println("\nTest 3: Zero Quantity Product");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 0, 15000.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("P002", "Clothing", 2, 2500.0, 0, 12.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(5000.0, bill.subtotal, "Subtotal excludes zero-qty item");
        assertTrue(bill.warnings.stream().anyMatch(w -> w.contains("Zero quantity")), "Warning for zero qty");
    }

    // Test 4: Negative quantity product
    static void testNegativeQuantity() {
        System.out.println("\nTest 4: Negative Quantity Product");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", -3, 15000.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 2, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(1000.0, bill.subtotal, "Subtotal excludes negative-qty item");
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Negative quantity")), "Error for negative qty");
    }

    // Test 5: Invalid product ID
    static void testInvalidProductId() {
        System.out.println("\nTest 5: Invalid Product ID");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("INVALID", "Electronics", 1, 100.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 1, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Invalid product ID")), "Error for invalid product");
        assertEqual(500.0, bill.subtotal, "Subtotal only from valid product");
    }

    // Test 6: All invalid products
    static void testAllInvalidProducts() {
        System.out.println("\nTest 6: All Invalid Products");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("INVALID1", "X", 1, 100.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("INVALID2", "Y", 1, 200.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("No valid items")), "Error: no valid items");
    }

    // Test 7: Invalid coupon code
    static void testInvalidCoupon() {
        System.out.println("\nTest 7: Invalid Coupon Code");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 1, 15000.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, "FAKECOUPON");
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Invalid coupon")), "Error for invalid coupon");
        assertEqual(0.0, bill.couponDiscount, "No coupon discount applied");
    }

    // Test 8: Valid coupon discount
    static void testValidCoupon() {
        System.out.println("\nTest 8: Valid Coupon (SAVE10)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 5, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, "SAVE10");
        // Subtotal = 2500, coupon = 10% of 2500 = 250, max is 500, so 250
        assertEqual(250.0, bill.couponDiscount, "Coupon discount = 10% of subtotal");
    }

    // Test 9: Coupon discount capped at maximum
    static void testCouponMaxDiscount() {
        System.out.println("\nTest 9: Coupon Discount Capped at Maximum");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P006", "Electronics", 1, 45000.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, "SAVE10");
        // 10% of 45000 = 4500 > maxDiscount 500 → capped at 500
        assertEqual(500.0, bill.couponDiscount, "Coupon capped at ₹500");
        assertTrue(bill.warnings.stream().anyMatch(w -> w.contains("capped")), "Warning about cap");
    }

    // Test 10: Out of stock product
    static void testOutOfStock() {
        System.out.println("\nTest 10: Out of Stock Product");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P008", "Clothing", 2, 1200.0, 0, 12.0));
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 1, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("out of stock")), "Error for out-of-stock");
        assertEqual(500.0, bill.subtotal, "Subtotal excludes out-of-stock");
    }

    // Test 11: Quantity exceeds stock (partial fulfillment)
    static void testQuantityExceedsStock() {
        System.out.println("\nTest 11: Quantity Exceeds Stock");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P006", "Electronics", 10, 45000.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        // Stock is 3, so adjusted to 3
        assertEqual(135000.0, bill.subtotal, "Subtotal = 3 * 45000");
        assertTrue(bill.warnings.stream().anyMatch(w -> w.contains("Adjusted")), "Warning about adjustment");
    }

    // Test 12: Free shipping threshold
    static void testFreeShipping() {
        System.out.println("\nTest 12: Free Shipping (order ≥ ₹2000)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 1, 15000.0, 0, 18.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(0.0, bill.shippingCharge, "Free shipping applied");
    }

    // Test 13: Shipping charge applied (below threshold)
    static void testShippingCharged() {
        System.out.println("\nTest 13: Shipping Charge (order < ₹2000)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P009", "Groceries", 1, 150.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(150.0, bill.shippingCharge, "Shipping charge of ₹150");
    }

    // Test 14: Bulk order discount (≥ 10 items)
    static void testBulkOrderDiscount() {
        System.out.println("\nTest 14: Bulk Order Discount (≥ 10 total quantity)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P009", "Groceries", 12, 150.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        // Subtotal = 12 * 150 = 1800, bulk discount = 5% of 1800 = 90
        assertEqual(1800.0, bill.subtotal, "Subtotal");
        assertEqual(90.0, bill.bulkDiscount, "Bulk discount = 5% of 1800");
    }

    // Test 15: No bulk discount (< 10 items)
    static void testNoBulkDiscount() {
        System.out.println("\nTest 15: No Bulk Discount (< 10 total quantity)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 3, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(0.0, bill.bulkDiscount, "No bulk discount");
    }

    // Test 16: Tax (GST) calculation
    static void testTaxCalculation() {
        System.out.println("\nTest 16: Tax (GST) Calculation");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 1, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        // Subtotal = 500, catDiscount = 2% = 10, taxable = 490, GST = 5% of 490 = 24.5
        assertEqual(24.5, bill.gstAmount, "GST = 5% of taxable");
    }

    // Test 17: Maximum discount limit
    static void testMaxDiscountLimit() {
        System.out.println("\nTest 17: Maximum Discount Limit (40%)");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        // High item discount + coupon + category discount to trigger cap
        items.add(new OrderManagement.OrderItem("P004", "Books", 10, 800.0, 20, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, "MEGA50");
        // Subtotal = 8000. cat=15%=1200, bulk=5%=400, item=20%=1600, coupon=50%=4000(capped 2000)
        // total = 1200+400+1600+2000 = 5200 > 40% of 8000 = 3200 → capped at 3200
        assertEqual(3200.0, bill.totalDiscount, "Total discount capped at 40%");
    }

    // Test 18: Empty order
    static void testEmptyOrder() {
        System.out.println("\nTest 18: Empty Order");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("no items")), "Error for empty order");
    }

    // Test 19: Null order
    static void testNullOrder() {
        System.out.println("\nTest 19: Null Order");
        OrderManagement.OrderBill bill = OrderManagement.processOrder(null, null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("no items")), "Error for null order");
    }

    // Test 20: Category-specific discount (Clothing 10%)
    static void testCategoryDiscountClothing() {
        System.out.println("\nTest 20: Category Discount — Clothing 10%");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P002", "Clothing", 2, 2500.0, 0, 12.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(500.0, bill.categoryDiscount, "Category discount = 10% of 5000");
    }

    // Test 21: Multiple categories in one order
    static void testMultipleCategoryDiscounts() {
        System.out.println("\nTest 21: Multiple Categories");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 1, 15000.0, 0, 18.0));
        items.add(new OrderManagement.OrderItem("P004", "Books", 1, 800.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        // Electronics 5% of 15000=750, Books 15% of 800=120 → total = 870
        assertEqual(870.0, bill.categoryDiscount, "Combined category discounts");
    }

    // Test 22: Item-level discount
    static void testItemLevelDiscount() {
        System.out.println("\nTest 22: Item-Level Discount");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 5, 500.0, 10, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        // Subtotal = 2500, item discount = 10% of 2500 = 250
        // Category discount = 2% of 2500 = 50
        // Total discount = 50 + 250 = 300
        assertEqual(300.0, bill.totalDiscount, "Total discount includes item-level");
    }

    // Test 23: Mixed valid and invalid items
    static void testMixedValidInvalid() {
        System.out.println("\nTest 23: Mix of Valid, Invalid, Zero-Qty, Out-of-Stock");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("BADID", "X", 1, 100.0, 0, 0));
        items.add(new OrderManagement.OrderItem("P008", "Clothing", 1, 1200.0, 0, 12.0)); // out of stock
        items.add(new OrderManagement.OrderItem("P001", "Electronics", 0, 15000.0, 0, 18.0)); // zero qty
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 2, 500.0, 0, 5.0)); // valid
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        assertEqual(1000.0, bill.subtotal, "Only valid item counted");
        assertTrue(bill.errors.size() >= 2, "Multiple errors");
        assertTrue(bill.warnings.size() >= 1, "At least one warning");
    }

    // Test 24: FLAT20 coupon
    static void testFlat20Coupon() {
        System.out.println("\nTest 24: FLAT20 Coupon");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P003", "Groceries", 2, 500.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, "FLAT20");
        // Subtotal = 1000, coupon = 20% of 1000 = 200, max = 1000, so 200
        assertEqual(200.0, bill.couponDiscount, "FLAT20 coupon = 20% of 1000");
    }

    // Test 25: Final amount calculation correctness
    static void testFinalAmountCalculation() {
        System.out.println("\nTest 25: Final Amount = Taxable + GST + Shipping");
        List<OrderManagement.OrderItem> items = new ArrayList<>();
        items.add(new OrderManagement.OrderItem("P009", "Groceries", 2, 150.0, 0, 5.0));
        OrderManagement.OrderBill bill = OrderManagement.processOrder(items, null);
        double expectedFinal = bill.taxableAmount + bill.gstAmount + bill.shippingCharge;
        assertEqual(Math.round(expectedFinal * 100.0) / 100.0, bill.finalAmount, "Final amount formula");
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" OrderManagement QA Test Suite");
        System.out.println("========================================");

        testSingleProduct();
        testMultipleProducts();
        testZeroQuantity();
        testNegativeQuantity();
        testInvalidProductId();
        testAllInvalidProducts();
        testInvalidCoupon();
        testValidCoupon();
        testCouponMaxDiscount();
        testOutOfStock();
        testQuantityExceedsStock();
        testFreeShipping();
        testShippingCharged();
        testBulkOrderDiscount();
        testNoBulkDiscount();
        testTaxCalculation();
        testMaxDiscountLimit();
        testEmptyOrder();
        testNullOrder();
        testCategoryDiscountClothing();
        testMultipleCategoryDiscounts();
        testItemLevelDiscount();
        testMixedValidInvalid();
        testFlat20Coupon();
        testFinalAmountCalculation();

        System.out.println("\n========================================");
        System.out.println(" Results: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println(" Total:   " + (testsPassed + testsFailed) + " tests");
        System.out.println("========================================");
    }
}
