import java.util.*;
import java.time.LocalDate;

/**
 * QA Test Suite for Airline Reservation System
 * Tests 25+ scenarios including booking, cancellation, refund, edge cases.
 */
public class AirlineReservationQA {

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
        AirlineReservation.initializeFlights();
    }

    // ==================== TEST CASES ====================

    // Test 1: Successful economy booking
    static void testSuccessfulBooking() {
        System.out.println("\nTest 1: Successful Economy Booking");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T1", "Test User", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertTrue(r.success, "Booking successful");
        assertTrue(r.booking != null, "Booking object created");
        assertTrue(r.booking.fare > 0, "Fare is positive");
    }

    // Test 2: Double booking prevention
    static void testDoubleBooking() {
        System.out.println("\nTest 2: Double Booking Prevention");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T2", "Test User", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.bookFlight("AI101", p, AirlineReservation.SeatClass.ECONOMY,
                LocalDate.of(2026, 8, 20), 10);
        AirlineReservation.BookingResult r2 = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertTrue(!r2.success, "Double booking rejected");
        assertTrue(r2.message.contains("already booked"), "Correct error message");
    }

    // Test 3: Cancellation with refund
    static void testCancellationRefund() {
        System.out.println("\nTest 3: Cancellation with Refund");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T3", "Test User", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult book = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        // Cancel 5 days before travel (travel = Aug 27, cancel = Aug 22) → 70%
        AirlineReservation.BookingResult cancel = AirlineReservation.cancelBooking(
                book.booking.bookingId, LocalDate.of(2026, 8, 22));
        assertTrue(cancel.success, "Cancellation successful");
        assertTrue(cancel.refundAmount > 0, "Refund amount > 0");
        double expectedRefund = Math.round(book.booking.totalAmount * 0.70 * 100.0) / 100.0;
        assertEqual(expectedRefund, cancel.refundAmount, "70% refund");
    }

    // Test 4: Full refund (>7 days before travel)
    static void testFullRefund() {
        System.out.println("\nTest 4: 90% Refund (>7 days before)");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T4", "Test User", 30,
                AirlineReservation.PassengerType.ADULT);
        // AI303 travels Aug 20 + 14 = Sep 3
        AirlineReservation.BookingResult book = AirlineReservation.bookFlight("AI303", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        // Cancel on Aug 20 → 14 days before → 90%
        AirlineReservation.BookingResult cancel = AirlineReservation.cancelBooking(
                book.booking.bookingId, LocalDate.of(2026, 8, 20));
        double expectedRefund = Math.round(book.booking.totalAmount * 0.90 * 100.0) / 100.0;
        assertEqual(expectedRefund, cancel.refundAmount, "90% refund");
    }

    // Test 5: Fully booked flight
    static void testFullyBookedFlight() {
        System.out.println("\nTest 5: Fully Booked Flight");
        resetSystem();
        // AI303 has 2 economy seats
        for (int i = 0; i < 2; i++) {
            AirlineReservation.Passenger p = new AirlineReservation.Passenger("F" + i, "User " + i, 30,
                    AirlineReservation.PassengerType.ADULT);
            AirlineReservation.bookFlight("AI303", p, AirlineReservation.SeatClass.ECONOMY,
                    LocalDate.of(2026, 8, 20), 10);
        }
        AirlineReservation.Passenger extra = new AirlineReservation.Passenger("F99", "Extra", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI303", extra,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertTrue(!r.success, "Booking rejected — fully booked");
        assertTrue(r.message.contains("No"), "Correct rejection message");
    }

    // Test 6: Invalid passenger
    static void testInvalidPassenger() {
        System.out.println("\nTest 6: Invalid Passenger (null)");
        resetSystem();
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", null,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertTrue(!r.success, "Booking rejected — null passenger");
    }

    // Test 7: Invalid flight ID
    static void testInvalidFlight() {
        System.out.println("\nTest 7: Invalid Flight ID");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T7", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("XX999", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertTrue(!r.success, "Booking rejected — invalid flight");
    }

    // Test 8: Excess baggage charge
    static void testExcessBaggage() {
        System.out.println("\nTest 8: Excess Baggage Charge");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T8", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 25);
        // Free = 15 kg, excess = 10 kg × 300 = 3000
        assertTrue(r.success, "Booking successful");
        assertEqual(3000.0, r.booking.baggageCharge, "Excess baggage = 10kg × 300");
    }

    // Test 9: No excess baggage (within limit)
    static void testNoBaggageCharge() {
        System.out.println("\nTest 9: No Baggage Charge (within limit)");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T9", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        assertEqual(0.0, r.booking.baggageCharge, "No baggage charge");
    }

    // Test 10: Business class fare multiplier
    static void testBusinessClassFare() {
        System.out.println("\nTest 10: Business Class Fare (2.5x)");
        resetSystem();
        AirlineReservation.Passenger p1 = new AirlineReservation.Passenger("T10A", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.Passenger p2 = new AirlineReservation.Passenger("T10B", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult eco = AirlineReservation.bookFlight("AI101", p1,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.BookingResult biz = AirlineReservation.bookFlight("AI101", p2,
                AirlineReservation.SeatClass.BUSINESS, LocalDate.of(2026, 8, 20), 0);
        assertTrue(biz.booking.fare > eco.booking.fare, "Business > Economy fare");
    }

    // Test 11: First class fare multiplier
    static void testFirstClassFare() {
        System.out.println("\nTest 11: First Class Fare (5x)");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T11", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.FIRST_CLASS, LocalDate.of(2026, 8, 20), 0);
        assertTrue(r.success, "First class booking");
        assertTrue(r.booking.fare >= 25000, "First class fare >= 5x base");
    }

    // Test 12: Child fare (50% of adult)
    static void testChildFare() {
        System.out.println("\nTest 12: Child Fare (50%)");
        resetSystem();
        AirlineReservation.Passenger adult = new AirlineReservation.Passenger("T12A", "Adult", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.Passenger child = new AirlineReservation.Passenger("T12B", "Child", 8,
                AirlineReservation.PassengerType.CHILD);
        AirlineReservation.BookingResult rAdult = AirlineReservation.bookFlight("AI101", adult,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.BookingResult rChild = AirlineReservation.bookFlight("AI101", child,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        double ratio = rChild.booking.fare / rAdult.booking.fare;
        assertTrue(Math.abs(ratio - 0.5) < 0.01, "Child fare = 50% of adult");
    }

    // Test 13: Senior fare (80% of adult)
    static void testSeniorFare() {
        System.out.println("\nTest 13: Senior Fare (80%)");
        resetSystem();
        AirlineReservation.Passenger adult = new AirlineReservation.Passenger("T13A", "Adult", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.Passenger senior = new AirlineReservation.Passenger("T13B", "Senior", 65,
                AirlineReservation.PassengerType.SENIOR);
        AirlineReservation.BookingResult rAdult = AirlineReservation.bookFlight("AI101", adult,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.BookingResult rSenior = AirlineReservation.bookFlight("AI101", senior,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        double ratio = rSenior.booking.fare / rAdult.booking.fare;
        assertTrue(Math.abs(ratio - 0.8) < 0.01, "Senior fare = 80% of adult");
    }

    // Test 14: Dynamic pricing — last-minute booking
    static void testDynamicPricingLastMinute() {
        System.out.println("\nTest 14: Dynamic Pricing — Last Minute (1.8x)");
        resetSystem();
        AirlineReservation.Passenger p1 = new AirlineReservation.Passenger("T14A", "Early", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.Passenger p2 = new AirlineReservation.Passenger("T14B", "Late", 30,
                AirlineReservation.PassengerType.ADULT);
        // AI202 travels Aug 23 (3 days from Aug 20)
        AirlineReservation.BookingResult early = AirlineReservation.bookFlight("AI202", p1,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 15), 0);
        AirlineReservation.BookingResult late = AirlineReservation.bookFlight("AI202", p2,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 22), 0);
        assertTrue(late.booking.fare > early.booking.fare, "Last-minute fare > early fare");
    }

    // Test 15: Cancellation of already cancelled booking
    static void testDoubleCancellation() {
        System.out.println("\nTest 15: Double Cancellation");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T15", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult book = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 10);
        AirlineReservation.cancelBooking(book.booking.bookingId, LocalDate.of(2026, 8, 21));
        AirlineReservation.BookingResult cancel2 = AirlineReservation.cancelBooking(
                book.booking.bookingId, LocalDate.of(2026, 8, 21));
        assertTrue(!cancel2.success, "Second cancellation rejected");
    }

    // Test 16: Cancel non-existent booking
    static void testCancelNonExistent() {
        System.out.println("\nTest 16: Cancel Non-Existent Booking");
        resetSystem();
        AirlineReservation.BookingResult r = AirlineReservation.cancelBooking("BK9999",
                LocalDate.of(2026, 8, 21));
        assertTrue(!r.success, "Cancellation rejected — booking not found");
    }

    // Test 17: Flight search — found
    static void testFlightSearchFound() {
        System.out.println("\nTest 17: Flight Search — Found");
        resetSystem();
        List<AirlineReservation.Flight> results = AirlineReservation.searchFlights("Delhi", "Mumbai");
        assertTrue(results.size() > 0, "Flights found");
        assertTrue(results.get(0).flightId.equals("AI101"), "Correct flight ID");
    }

    // Test 18: Flight search — not found
    static void testFlightSearchNotFound() {
        System.out.println("\nTest 18: Flight Search — Not Found");
        resetSystem();
        List<AirlineReservation.Flight> results = AirlineReservation.searchFlights("Delhi", "Tokyo");
        assertTrue(results.isEmpty(), "No flights found");
    }

    // Test 19: Zero seats in class (First Class AI303)
    static void testZeroSeatsClass() {
        System.out.println("\nTest 19: Zero Seats in Class");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T19", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI303", p,
                AirlineReservation.SeatClass.FIRST_CLASS, LocalDate.of(2026, 8, 20), 0);
        assertTrue(!r.success, "No first-class seats available");
    }

    // Test 20: Seat count decreases after booking
    static void testSeatCountDecrease() {
        System.out.println("\nTest 20: Seat Count Decreases After Booking");
        resetSystem();
        AirlineReservation.Flight f = AirlineReservation.flights.get("AI101");
        int before = f.getAvailableSeats(AirlineReservation.SeatClass.ECONOMY);
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T20", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.bookFlight("AI101", p, AirlineReservation.SeatClass.ECONOMY,
                LocalDate.of(2026, 8, 20), 0);
        int after = f.getAvailableSeats(AirlineReservation.SeatClass.ECONOMY);
        assertEqual(before - 1, after, "Seats decreased by 1");
    }

    // Test 21: Seat count increases after cancellation
    static void testSeatCountIncreaseAfterCancel() {
        System.out.println("\nTest 21: Seat Count Increases After Cancellation");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T21", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult book = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.Flight f = AirlineReservation.flights.get("AI101");
        int afterBook = f.getAvailableSeats(AirlineReservation.SeatClass.ECONOMY);
        AirlineReservation.cancelBooking(book.booking.bookingId, LocalDate.of(2026, 8, 21));
        int afterCancel = f.getAvailableSeats(AirlineReservation.SeatClass.ECONOMY);
        assertEqual(afterBook + 1, afterCancel, "Seats increased by 1");
    }

    // Test 22: Military discount (70% of adult)
    static void testMilitaryDiscount() {
        System.out.println("\nTest 22: Military Discount (70%)");
        resetSystem();
        AirlineReservation.Passenger adult = new AirlineReservation.Passenger("T22A", "Adult", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.Passenger military = new AirlineReservation.Passenger("T22B", "Military", 30,
                AirlineReservation.PassengerType.MILITARY);
        AirlineReservation.BookingResult rAdult = AirlineReservation.bookFlight("AI101", adult,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.BookingResult rMil = AirlineReservation.bookFlight("AI101", military,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        double ratio = rMil.booking.fare / rAdult.booking.fare;
        assertTrue(Math.abs(ratio - 0.7) < 0.01, "Military fare = 70% of adult");
    }

    // Test 23: Negative baggage weight
    static void testNegativeBaggage() {
        System.out.println("\nTest 23: Negative Baggage Weight");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T23", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), -5);
        assertTrue(!r.success, "Negative baggage rejected");
    }

    // Test 24: Booking after cancellation (re-booking)
    static void testRebooking() {
        System.out.println("\nTest 24: Re-Booking After Cancellation");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T24", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult book1 = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        AirlineReservation.cancelBooking(book1.booking.bookingId, LocalDate.of(2026, 8, 20));
        AirlineReservation.BookingResult book2 = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.ECONOMY, LocalDate.of(2026, 8, 20), 0);
        assertTrue(book2.success, "Re-booking after cancellation succeeds");
    }

    // Test 25: Business class free baggage (25kg)
    static void testBusinessFreeBaggage() {
        System.out.println("\nTest 25: Business Class Free Baggage (25kg)");
        resetSystem();
        AirlineReservation.Passenger p = new AirlineReservation.Passenger("T25", "Test", 30,
                AirlineReservation.PassengerType.ADULT);
        AirlineReservation.BookingResult r = AirlineReservation.bookFlight("AI101", p,
                AirlineReservation.SeatClass.BUSINESS, LocalDate.of(2026, 8, 20), 25);
        assertEqual(0.0, r.booking.baggageCharge, "No baggage charge at 25kg");
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" AirlineReservation QA Test Suite");
        System.out.println("==============================================");

        testSuccessfulBooking();
        testDoubleBooking();
        testCancellationRefund();
        testFullRefund();
        testFullyBookedFlight();
        testInvalidPassenger();
        testInvalidFlight();
        testExcessBaggage();
        testNoBaggageCharge();
        testBusinessClassFare();
        testFirstClassFare();
        testChildFare();
        testSeniorFare();
        testDynamicPricingLastMinute();
        testDoubleCancellation();
        testCancelNonExistent();
        testFlightSearchFound();
        testFlightSearchNotFound();
        testZeroSeatsClass();
        testSeatCountDecrease();
        testSeatCountIncreaseAfterCancel();
        testMilitaryDiscount();
        testNegativeBaggage();
        testRebooking();
        testBusinessFreeBaggage();

        System.out.println("\n==============================================");
        System.out.println(" Results: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println(" Total:   " + (testsPassed + testsFailed) + " tests");
        System.out.println("==============================================");
    }
}
