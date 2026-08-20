import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Airline Reservation System
 *
 * Implements:
 *   - Flight search
 *   - Seat availability
 *   - Passenger booking
 *   - Cancellation & refund
 *   - Baggage charges
 *   - Dynamic pricing based on seats, dates, passenger type, and class
 */
public class AirlineReservation {

    // ========================== ENUMS ==========================

    enum SeatClass {
        ECONOMY, BUSINESS, FIRST_CLASS
    }

    enum PassengerType {
        ADULT, CHILD, SENIOR, MILITARY, STUDENT
    }

    // ========================== DATA MODELS ==========================

    static class Flight {
        String flightId;
        String origin;
        String destination;
        LocalDate travelDate;
        Map<SeatClass, Integer> totalSeats;
        Map<SeatClass, Integer> availableSeats;
        Map<SeatClass, Double> baseFare;
        List<String> bookedPassengers; // list of booking IDs

        public Flight(String flightId, String origin, String destination, LocalDate travelDate,
                      Map<SeatClass, Integer> totalSeats, Map<SeatClass, Double> baseFare) {
            this.flightId = flightId;
            this.origin = origin;
            this.destination = destination;
            this.travelDate = travelDate;
            this.totalSeats = new EnumMap<>(totalSeats);
            this.availableSeats = new EnumMap<>(totalSeats);
            this.baseFare = new EnumMap<>(baseFare);
            this.bookedPassengers = new ArrayList<>();
        }

        int getAvailableSeats(SeatClass sc) {
            return availableSeats.getOrDefault(sc, 0);
        }
    }

    static class Passenger {
        String passengerId;
        String name;
        int age;
        PassengerType type;

        public Passenger(String passengerId, String name, int age, PassengerType type) {
            this.passengerId = passengerId;
            this.name = name;
            this.age = age;
            this.type = type;
        }
    }

    static class Booking {
        String bookingId;
        String flightId;
        Passenger passenger;
        SeatClass seatClass;
        double fare;
        double baggageCharge;
        double totalAmount;
        boolean cancelled;
        LocalDate bookingDate;
        int checkedBagsKg;

        public Booking(String bookingId, String flightId, Passenger passenger,
                       SeatClass seatClass, double fare, double baggageCharge,
                       double totalAmount, LocalDate bookingDate, int checkedBagsKg) {
            this.bookingId = bookingId;
            this.flightId = flightId;
            this.passenger = passenger;
            this.seatClass = seatClass;
            this.fare = fare;
            this.baggageCharge = baggageCharge;
            this.totalAmount = totalAmount;
            this.bookingDate = bookingDate;
            this.cancelled = false;
            this.checkedBagsKg = checkedBagsKg;
        }
    }

    static class BookingResult {
        boolean success;
        String message;
        Booking booking;
        double refundAmount;

        BookingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        BookingResult(boolean success, String message, Booking booking) {
            this.success = success;
            this.message = message;
            this.booking = booking;
        }
    }

    // ========================== CONSTANTS ==========================

    // Free baggage allowance (kg) per class
    static final Map<SeatClass, Integer> FREE_BAGGAGE = new EnumMap<>(SeatClass.class);
    static {
        FREE_BAGGAGE.put(SeatClass.ECONOMY, 15);
        FREE_BAGGAGE.put(SeatClass.BUSINESS, 25);
        FREE_BAGGAGE.put(SeatClass.FIRST_CLASS, 35);
    }

    static final double EXCESS_BAGGAGE_RATE_PER_KG = 300.0;

    // Passenger type fare multipliers
    static final Map<PassengerType, Double> PASSENGER_MULTIPLIER = new EnumMap<>(PassengerType.class);
    static {
        PASSENGER_MULTIPLIER.put(PassengerType.ADULT, 1.0);
        PASSENGER_MULTIPLIER.put(PassengerType.CHILD, 0.5);
        PASSENGER_MULTIPLIER.put(PassengerType.SENIOR, 0.8);
        PASSENGER_MULTIPLIER.put(PassengerType.MILITARY, 0.7);
        PASSENGER_MULTIPLIER.put(PassengerType.STUDENT, 0.85);
    }

    // Class multiplier on base fare
    static final Map<SeatClass, Double> CLASS_MULTIPLIER = new EnumMap<>(SeatClass.class);
    static {
        CLASS_MULTIPLIER.put(SeatClass.ECONOMY, 1.0);
        CLASS_MULTIPLIER.put(SeatClass.BUSINESS, 2.5);
        CLASS_MULTIPLIER.put(SeatClass.FIRST_CLASS, 5.0);
    }

    // ========================== SYSTEM STATE ==========================

    static Map<String, Flight> flights = new LinkedHashMap<>();
    static Map<String, Booking> bookings = new LinkedHashMap<>();
    static int bookingCounter = 0;

    // ========================== INITIALIZATION ==========================

    public static void initializeFlights() {
        flights.clear();
        bookings.clear();
        bookingCounter = 0;

        LocalDate today = LocalDate.of(2026, 8, 20);

        // Flight 1: DEL → BOM
        Map<SeatClass, Integer> seats1 = new EnumMap<>(SeatClass.class);
        seats1.put(SeatClass.ECONOMY, 100);
        seats1.put(SeatClass.BUSINESS, 20);
        seats1.put(SeatClass.FIRST_CLASS, 5);
        Map<SeatClass, Double> fares1 = new EnumMap<>(SeatClass.class);
        fares1.put(SeatClass.ECONOMY, 5000.0);
        fares1.put(SeatClass.BUSINESS, 5000.0);
        fares1.put(SeatClass.FIRST_CLASS, 5000.0);
        flights.put("AI101", new Flight("AI101", "Delhi", "Mumbai", today.plusDays(7), seats1, fares1));

        // Flight 2: BOM → BLR
        Map<SeatClass, Integer> seats2 = new EnumMap<>(SeatClass.class);
        seats2.put(SeatClass.ECONOMY, 80);
        seats2.put(SeatClass.BUSINESS, 15);
        seats2.put(SeatClass.FIRST_CLASS, 3);
        Map<SeatClass, Double> fares2 = new EnumMap<>(SeatClass.class);
        fares2.put(SeatClass.ECONOMY, 4000.0);
        fares2.put(SeatClass.BUSINESS, 4000.0);
        fares2.put(SeatClass.FIRST_CLASS, 4000.0);
        flights.put("AI202", new Flight("AI202", "Mumbai", "Bangalore", today.plusDays(3), seats2, fares2));

        // Flight 3: DEL → CCU (very few seats for testing)
        Map<SeatClass, Integer> seats3 = new EnumMap<>(SeatClass.class);
        seats3.put(SeatClass.ECONOMY, 2);
        seats3.put(SeatClass.BUSINESS, 1);
        seats3.put(SeatClass.FIRST_CLASS, 0);
        Map<SeatClass, Double> fares3 = new EnumMap<>(SeatClass.class);
        fares3.put(SeatClass.ECONOMY, 6000.0);
        fares3.put(SeatClass.BUSINESS, 6000.0);
        fares3.put(SeatClass.FIRST_CLASS, 6000.0);
        flights.put("AI303", new Flight("AI303", "Delhi", "Kolkata", today.plusDays(14), seats3, fares3));
    }

    // ========================== CORE LOGIC ==========================

    /**
     * Searches for flights between origin and destination.
     */
    public static List<Flight> searchFlights(String origin, String destination) {
        List<Flight> results = new ArrayList<>();
        for (Flight f : flights.values()) {
            if (f.origin.equalsIgnoreCase(origin) && f.destination.equalsIgnoreCase(destination)) {
                results.add(f);
            }
        }
        return results;
    }

    /**
     * Calculates dynamic fare based on availability, booking date, travel date, etc.
     */
    public static double calculateDynamicFare(Flight flight, SeatClass seatClass,
                                               PassengerType passengerType, LocalDate bookingDate) {
        double baseFare = flight.baseFare.getOrDefault(seatClass, 5000.0);

        // Class multiplier
        double classMul = CLASS_MULTIPLIER.getOrDefault(seatClass, 1.0);
        double fare = baseFare * classMul;

        // Passenger type multiplier
        double passengerMul = PASSENGER_MULTIPLIER.getOrDefault(passengerType, 1.0);
        fare *= passengerMul;

        // Seat availability factor: fewer seats → higher price
        int totalSeats = flight.totalSeats.getOrDefault(seatClass, 1);
        int availSeats = flight.availableSeats.getOrDefault(seatClass, 0);
        if (totalSeats > 0) {
            double occupancy = 1.0 - ((double) availSeats / totalSeats);
            if (occupancy > 0.8) {
                fare *= 1.5; // 50% surge when >80% booked
            } else if (occupancy > 0.6) {
                fare *= 1.25; // 25% surge when >60% booked
            }
        }

        // Booking date factor: closer to travel → more expensive
        long daysUntilTravel = ChronoUnit.DAYS.between(bookingDate, flight.travelDate);
        if (daysUntilTravel <= 1) {
            fare *= 1.8; // last-minute
        } else if (daysUntilTravel <= 3) {
            fare *= 1.4;
        } else if (daysUntilTravel <= 7) {
            fare *= 1.15;
        }
        // > 7 days: no surcharge

        return Math.round(fare * 100.0) / 100.0;
    }

    /**
     * Calculates baggage charge.
     */
    public static double calculateBaggageCharge(SeatClass seatClass, int checkedBagsKg) {
        int freeKg = FREE_BAGGAGE.getOrDefault(seatClass, 15);
        if (checkedBagsKg <= freeKg) return 0;
        return (checkedBagsKg - freeKg) * EXCESS_BAGGAGE_RATE_PER_KG;
    }

    /**
     * Books a passenger on a flight.
     */
    public static BookingResult bookFlight(String flightId, Passenger passenger,
                                            SeatClass seatClass, LocalDate bookingDate,
                                            int checkedBagsKg) {
        // Validate flight
        Flight flight = flights.get(flightId);
        if (flight == null) {
            return new BookingResult(false, "Flight not found: " + flightId);
        }

        // Validate passenger
        if (passenger == null || passenger.passengerId == null || passenger.name == null
                || passenger.name.isEmpty()) {
            return new BookingResult(false, "Invalid passenger details.");
        }

        // Check for duplicate booking
        for (Booking b : bookings.values()) {
            if (!b.cancelled && b.flightId.equals(flightId)
                    && b.passenger.passengerId.equals(passenger.passengerId)) {
                return new BookingResult(false,
                        "Passenger " + passenger.passengerId + " is already booked on flight " + flightId);
            }
        }

        // Check seat availability
        int available = flight.getAvailableSeats(seatClass);
        if (available <= 0) {
            return new BookingResult(false,
                    "No " + seatClass + " seats available on flight " + flightId);
        }

        // Validate baggage
        if (checkedBagsKg < 0) {
            return new BookingResult(false, "Baggage weight cannot be negative.");
        }

        // Calculate fare & baggage
        double fare = calculateDynamicFare(flight, seatClass, passenger.type, bookingDate);
        double baggageCharge = calculateBaggageCharge(seatClass, checkedBagsKg);
        double totalAmount = fare + baggageCharge;

        // Create booking
        bookingCounter++;
        String bookingId = "BK" + String.format("%04d", bookingCounter);
        Booking booking = new Booking(bookingId, flightId, passenger, seatClass,
                fare, baggageCharge, totalAmount, bookingDate, checkedBagsKg);

        // Update seats
        flight.availableSeats.put(seatClass, available - 1);
        flight.bookedPassengers.add(bookingId);
        bookings.put(bookingId, booking);

        return new BookingResult(true,
                "Booking confirmed! ID: " + bookingId + ", Total: ₹" + totalAmount, booking);
    }

    /**
     * Cancels a booking and calculates refund.
     */
    public static BookingResult cancelBooking(String bookingId, LocalDate cancellationDate) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            return new BookingResult(false, "Booking not found: " + bookingId);
        }
        if (booking.cancelled) {
            return new BookingResult(false, "Booking " + bookingId + " is already cancelled.");
        }

        Flight flight = flights.get(booking.flightId);

        // Calculate refund based on cancellation timing
        long daysBeforeTravel = ChronoUnit.DAYS.between(cancellationDate, flight.travelDate);
        double refundPercent;
        if (daysBeforeTravel > 7) {
            refundPercent = 0.90; // 90% refund
        } else if (daysBeforeTravel > 3) {
            refundPercent = 0.70; // 70% refund
        } else if (daysBeforeTravel > 1) {
            refundPercent = 0.50; // 50% refund
        } else if (daysBeforeTravel >= 0) {
            refundPercent = 0.25; // 25% refund (same-day/next-day)
        } else {
            refundPercent = 0; // past travel date
        }

        double refundAmount = Math.round(booking.totalAmount * refundPercent * 100.0) / 100.0;

        // Update state
        booking.cancelled = true;
        flight.availableSeats.put(booking.seatClass,
                flight.availableSeats.get(booking.seatClass) + 1);

        BookingResult result = new BookingResult(true,
                "Booking " + bookingId + " cancelled. Refund: ₹" + refundAmount
                        + " (" + (refundPercent * 100) + "%)");
        result.refundAmount = refundAmount;
        return result;
    }

    // ========================== MAIN ==========================

    public static void main(String[] args) {
        System.out.println("=== Airline Reservation System ===\n");

        initializeFlights();

        // Search flights
        List<Flight> results = searchFlights("Delhi", "Mumbai");
        System.out.println("Flights from Delhi to Mumbai: " + results.size());
        for (Flight f : results) {
            System.out.println("  " + f.flightId + " on " + f.travelDate
                    + " | Economy seats: " + f.getAvailableSeats(SeatClass.ECONOMY));
        }

        // Book a flight
        Passenger p1 = new Passenger("PX001", "Amit Kumar", 30, PassengerType.ADULT);
        BookingResult r1 = bookFlight("AI101", p1, SeatClass.ECONOMY,
                LocalDate.of(2026, 8, 20), 20);
        System.out.println("\n" + r1.message);
        if (r1.booking != null) {
            System.out.println("  Fare: ₹" + r1.booking.fare + " | Baggage: ₹" + r1.booking.baggageCharge);
        }

        // Book business class
        Passenger p2 = new Passenger("PX002", "Priya Shah", 25, PassengerType.STUDENT);
        BookingResult r2 = bookFlight("AI101", p2, SeatClass.BUSINESS,
                LocalDate.of(2026, 8, 20), 10);
        System.out.println("\n" + r2.message);

        // Cancel booking
        if (r1.booking != null) {
            BookingResult cancel = cancelBooking(r1.booking.bookingId, LocalDate.of(2026, 8, 22));
            System.out.println("\n" + cancel.message);
        }

        // Try double booking
        BookingResult r3 = bookFlight("AI101", p2, SeatClass.BUSINESS,
                LocalDate.of(2026, 8, 20), 10);
        System.out.println("\n" + r3.message);
    }
}
