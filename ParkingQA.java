import java.util.*;
import java.time.LocalDateTime;

/**
 * QA Test Suite for Smart Parking Management System
 * Tests 25+ scenarios covering all features and edge cases.
 */
public class ParkingQA {

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

    static void resetSystem() {
        ParkingManagement.initializeParkingLot();
    }

    // ==================== TEST CASES ====================

    // Test 1: Successful car entry
    static void testCarEntry() {
        System.out.println("\nTest 1: Successful Car Entry");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("CAR001", ParkingManagement.VehicleType.CAR, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "Car parked successfully");
        assertTrue(r.ticket != null, "Ticket generated");
    }

    // Test 2: Successful bike entry
    static void testBikeEntry() {
        System.out.println("\nTest 2: Successful Bike Entry");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("BIKE001", ParkingManagement.VehicleType.BIKE, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "Bike parked");
        assertTrue(r.ticket.slotId.startsWith("S"), "Assigned small slot");
    }

    // Test 3: Truck entry
    static void testTruckEntry() {
        System.out.println("\nTest 3: Successful Truck Entry");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("TRK001", ParkingManagement.VehicleType.TRUCK, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "Truck parked");
        assertTrue(r.ticket.slotId.startsWith("XL"), "Assigned XLarge slot");
    }

    // Test 4: SUV entry
    static void testSUVEntry() {
        System.out.println("\nTest 4: Successful SUV Entry");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("SUV001", ParkingManagement.VehicleType.SUV, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "SUV parked");
        assertTrue(r.ticket.slotId.startsWith("L"), "Assigned large slot");
    }

    // Test 5: EV entry with charger
    static void testEVEntry() {
        System.out.println("\nTest 5: EV Entry (Charger Slot)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("EV001", ParkingManagement.VehicleType.ELECTRIC_VEHICLE, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "EV parked");
        assertTrue(r.ticket.useEVCharger, "EV charger slot assigned");
    }

    // Test 6: Full parking lot (bikes)
    static void testFullParkingLot() {
        System.out.println("\nTest 6: Full Parking Lot (Bikes)");
        resetSystem();
        // Fill all 10 small slots
        for (int i = 1; i <= 10; i++) {
            ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("B" + i, ParkingManagement.VehicleType.BIKE, false);
            ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        }
        ParkingManagement.Vehicle extra = new ParkingManagement.Vehicle("B99", ParkingManagement.VehicleType.BIKE, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(extra, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(!r.success, "Full parking lot rejected");
        assertTrue(r.message.contains("No available"), "Correct rejection message");
    }

    // Test 7: Duplicate vehicle entry
    static void testDuplicateVehicle() {
        System.out.println("\nTest 7: Duplicate Vehicle Entry");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("DUP001", ParkingManagement.VehicleType.CAR, false);
        ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        ParkingManagement.ParkingResult r2 = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 11, 0));
        assertTrue(!r2.success, "Duplicate vehicle rejected");
        assertTrue(r2.message.contains("already parked"), "Correct error message");
    }

    // Test 8: Vehicle exit — normal fee
    static void testNormalExit() {
        System.out.println("\nTest 8: Normal Exit (2 hours, non-peak)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("EXIT001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0); // 6 AM (non-peak)
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(2);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        assertTrue(exitR.success, "Exit successful");
        // 2 hours × 40 = 80 (non-peak, non-VIP)
        assertEqual(80.0, exitR.fee, "Fee = 2h × 40");
    }

    // Test 9: Peak-hour pricing
    static void testPeakHourPricing() {
        System.out.println("\nTest 9: Peak-Hour Pricing (1.5x)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("PEAK001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 10, 0); // 10 AM (peak)
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(2);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        // 2h × 40 × 1.5 = 120
        assertEqual(120.0, exitR.fee, "Peak fee = 2h × 40 × 1.5");
    }

    // Test 10: VIP parking premium
    static void testVIPPremium() {
        System.out.println("\nTest 10: VIP Parking Premium (1.25x)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("VIP001", ParkingManagement.VehicleType.CAR, true);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0); // non-peak
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(2);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        // 2h × 40 × 1.25 = 100
        assertEqual(100.0, exitR.fee, "VIP fee = 2h × 40 × 1.25");
    }

    // Test 11: Lost ticket with penalty
    static void testLostTicket() {
        System.out.println("\nTest 11: Lost Ticket Penalty (₹500)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("LOST001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0);
        ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(2);
        ParkingManagement.ParkingResult r = ParkingManagement.handleLostTicket("LOST001", exit);
        assertTrue(r.success, "Lost ticket handled");
        // Fee = 2h × 40 + 500 penalty = 580
        assertEqual(580.0, r.fee, "Lost ticket fee = parking + 500 penalty");
    }

    // Test 12: Lost ticket — vehicle not found
    static void testLostTicketNotFound() {
        System.out.println("\nTest 12: Lost Ticket — Vehicle Not Found");
        resetSystem();
        ParkingManagement.ParkingResult r = ParkingManagement.handleLostTicket("UNKNOWN",
                LocalDateTime.of(2026, 8, 20, 12, 0));
        assertTrue(!r.success, "Vehicle not found");
    }

    // Test 13: Early exit (< 15 minutes)
    static void testEarlyExit() {
        System.out.println("\nTest 13: Early Exit (< 15 min = Minimum Fee)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("EARLY001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 10, 0);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusMinutes(10);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        assertEqual(10.0, exitR.fee, "Minimum fee for early exit");
    }

    // Test 14: Overnight parking
    static void testOvernightParking() {
        System.out.println("\nTest 14: Overnight Parking (24h+)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("NIGHT001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0); // 6 AM non-peak
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(25); // 25 hours = 1 night
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        // 25h × 40 + 1 night × 200 = 1000 + 200 = 1200
        assertEqual(1200.0, exitR.fee, "Overnight fee = 25h × 40 + 200");
    }

    // Test 15: EV charging fee
    static void testEVChargingFee() {
        System.out.println("\nTest 15: EV Charging Fee");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("EV002", ParkingManagement.VehicleType.ELECTRIC_VEHICLE, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0); // non-peak
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(3);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        // Parking: 3h × 50 = 150, EV charging: 3h × 30 = 90 → total = 240
        assertEqual(240.0, exitR.fee, "EV fee = parking 150 + charging 90");
    }

    // Test 16: Wrong vehicle-slot combination (system should prevent)
    static void testWrongVehicleSlot() {
        System.out.println("\nTest 16: Wrong Vehicle-Slot (Bike gets Small, not Medium)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("BIKE002", ParkingManagement.VehicleType.BIKE, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(r.success, "Bike parked");
        assertTrue(r.ticket.slotId.startsWith("S"), "Bike gets SMALL slot, not MEDIUM");
    }

    // Test 17: Invalid vehicle (null)
    static void testInvalidVehicle() {
        System.out.println("\nTest 17: Invalid Vehicle (null)");
        resetSystem();
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(null, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(!r.success, "Null vehicle rejected");
    }

    // Test 18: Empty license plate
    static void testEmptyLicensePlate() {
        System.out.println("\nTest 18: Empty License Plate");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("", ParkingManagement.VehicleType.CAR, false);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        assertTrue(!r.success, "Empty license plate rejected");
    }

    // Test 19: Exit before entry time
    static void testExitBeforeEntry() {
        System.out.println("\nTest 19: Exit Before Entry Time");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("TIME001", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 10, 0);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.minusHours(1); // before entry
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        assertTrue(!exitR.success, "Exit before entry rejected");
    }

    // Test 20: Exit with invalid ticket ID
    static void testInvalidTicket() {
        System.out.println("\nTest 20: Exit with Invalid Ticket");
        resetSystem();
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleExit("TK9999",
                LocalDateTime.of(2026, 8, 20, 12, 0));
        assertTrue(!r.success, "Invalid ticket rejected");
    }

    // Test 21: Bike hourly rate
    static void testBikeHourlyRate() {
        System.out.println("\nTest 21: Bike Hourly Rate");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("BIKEFEE", ParkingManagement.VehicleType.BIKE, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, entry.plusHours(1));
        // 1h × 20 = 20
        assertEqual(20.0, exitR.fee, "Bike rate = ₹20/h");
    }

    // Test 22: Truck hourly rate
    static void testTruckHourlyRate() {
        System.out.println("\nTest 22: Truck Hourly Rate");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("TRKFEE", ParkingManagement.VehicleType.TRUCK, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 6, 0);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, entry.plusHours(1));
        // 1h × 80 = 80
        assertEqual(80.0, exitR.fee, "Truck rate = ₹80/h");
    }

    // Test 23: Availability decreases after entry
    static void testAvailabilityDecrease() {
        System.out.println("\nTest 23: Availability Decreases After Entry");
        resetSystem();
        int before = ParkingManagement.getAvailability().get(ParkingManagement.SlotSize.MEDIUM);
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("AVAIL01", ParkingManagement.VehicleType.CAR, false);
        ParkingManagement.vehicleEntry(v, LocalDateTime.of(2026, 8, 20, 10, 0));
        int after = ParkingManagement.getAvailability().get(ParkingManagement.SlotSize.MEDIUM);
        assertEqual(before - 1, after, "Available medium slots decreased by 1");
    }

    // Test 24: Availability increases after exit
    static void testAvailabilityIncrease() {
        System.out.println("\nTest 24: Availability Increases After Exit");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("AVAIL02", ParkingManagement.VehicleType.CAR, false);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 10, 0);
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        int afterEntry = ParkingManagement.getAvailability().get(ParkingManagement.SlotSize.MEDIUM);
        ParkingManagement.vehicleExit(r.ticket.ticketId, entry.plusHours(1));
        int afterExit = ParkingManagement.getAvailability().get(ParkingManagement.SlotSize.MEDIUM);
        assertEqual(afterEntry + 1, afterExit, "Available medium slots increased by 1");
    }

    // Test 25: Peak + VIP + Overnight combined
    static void testCombinedPricing() {
        System.out.println("\nTest 25: Combined Pricing (Peak + VIP + Overnight)");
        resetSystem();
        ParkingManagement.Vehicle v = new ParkingManagement.Vehicle("COMBO01", ParkingManagement.VehicleType.SUV, true);
        LocalDateTime entry = LocalDateTime.of(2026, 8, 20, 12, 0); // peak
        ParkingManagement.ParkingResult r = ParkingManagement.vehicleEntry(v, entry);
        LocalDateTime exit = entry.plusHours(26); // 26 hours = 1 night
        ParkingManagement.ParkingResult exitR = ParkingManagement.vehicleExit(r.ticket.ticketId, exit);
        // 26h × 60 × 1.5 (peak) × 1.25 (VIP) + 200 (1 night) = 26×60×1.875 + 200 = 2925 + 200 = 3125
        assertEqual(3125.0, exitR.fee, "Combined: peak + VIP + overnight");
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" ParkingManagement QA Test Suite");
        System.out.println("==============================================");

        testCarEntry();
        testBikeEntry();
        testTruckEntry();
        testSUVEntry();
        testEVEntry();
        testFullParkingLot();
        testDuplicateVehicle();
        testNormalExit();
        testPeakHourPricing();
        testVIPPremium();
        testLostTicket();
        testLostTicketNotFound();
        testEarlyExit();
        testOvernightParking();
        testEVChargingFee();
        testWrongVehicleSlot();
        testInvalidVehicle();
        testEmptyLicensePlate();
        testExitBeforeEntry();
        testInvalidTicket();
        testBikeHourlyRate();
        testTruckHourlyRate();
        testAvailabilityDecrease();
        testAvailabilityIncrease();
        testCombinedPricing();

        System.out.println("\n==============================================");
        System.out.println(" Results: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println(" Total:   " + (testsPassed + testsFailed) + " tests");
        System.out.println("==============================================");
    }
}
