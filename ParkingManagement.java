import java.util.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Smart Parking Management System
 *
 * Implements:
 *   - Vehicle entry & exit
 *   - Parking slot allocation by vehicle type
 *   - Dynamic parking fee
 *   - Lost-ticket handling
 *   - VIP parking
 *   - Peak-hour pricing
 *   - EV charging fee
 *
 * Vehicle types: Bike, Car, SUV, Truck, Electric Vehicle (EV)
 */
public class ParkingManagement {

    // ========================== ENUMS ==========================

    enum VehicleType {
        BIKE, CAR, SUV, TRUCK, ELECTRIC_VEHICLE
    }

    enum SlotSize {
        SMALL,   // Bike
        MEDIUM,  // Car, EV
        LARGE,   // SUV
        XLARGE   // Truck
    }

    // ========================== DATA MODELS ==========================

    static class ParkingSlot {
        String slotId;
        SlotSize size;
        boolean isVIP;
        boolean isOccupied;
        boolean hasEVCharger;

        public ParkingSlot(String slotId, SlotSize size, boolean isVIP, boolean hasEVCharger) {
            this.slotId = slotId;
            this.size = size;
            this.isVIP = isVIP;
            this.isOccupied = false;
            this.hasEVCharger = hasEVCharger;
        }
    }

    static class Vehicle {
        String licensePlate;
        VehicleType type;
        boolean isVIP;

        public Vehicle(String licensePlate, VehicleType type, boolean isVIP) {
            this.licensePlate = licensePlate;
            this.type = type;
            this.isVIP = isVIP;
        }
    }

    static class ParkingTicket {
        String ticketId;
        String licensePlate;
        VehicleType vehicleType;
        String slotId;
        LocalDateTime entryTime;
        LocalDateTime exitTime;
        boolean isVIP;
        boolean isLost;
        boolean useEVCharger;

        public ParkingTicket(String ticketId, String licensePlate, VehicleType vehicleType,
                             String slotId, LocalDateTime entryTime, boolean isVIP, boolean useEVCharger) {
            this.ticketId = ticketId;
            this.licensePlate = licensePlate;
            this.vehicleType = vehicleType;
            this.slotId = slotId;
            this.entryTime = entryTime;
            this.isVIP = isVIP;
            this.isLost = false;
            this.useEVCharger = useEVCharger;
        }
    }

    static class ParkingResult {
        boolean success;
        String message;
        ParkingTicket ticket;
        double fee;

        ParkingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        ParkingResult(boolean success, String message, ParkingTicket ticket) {
            this.success = success;
            this.message = message;
            this.ticket = ticket;
        }
    }

    // ========================== CONSTANTS ==========================

    // Hourly rates by vehicle type (base rate, non-peak)
    static final Map<VehicleType, Double> HOURLY_RATE = new EnumMap<>(VehicleType.class);
    static {
        HOURLY_RATE.put(VehicleType.BIKE, 20.0);
        HOURLY_RATE.put(VehicleType.CAR, 40.0);
        HOURLY_RATE.put(VehicleType.SUV, 60.0);
        HOURLY_RATE.put(VehicleType.TRUCK, 80.0);
        HOURLY_RATE.put(VehicleType.ELECTRIC_VEHICLE, 50.0);
    }

    // Vehicle type → required slot size
    static final Map<VehicleType, SlotSize> VEHICLE_SLOT_MAP = new EnumMap<>(VehicleType.class);
    static {
        VEHICLE_SLOT_MAP.put(VehicleType.BIKE, SlotSize.SMALL);
        VEHICLE_SLOT_MAP.put(VehicleType.CAR, SlotSize.MEDIUM);
        VEHICLE_SLOT_MAP.put(VehicleType.SUV, SlotSize.LARGE);
        VEHICLE_SLOT_MAP.put(VehicleType.TRUCK, SlotSize.XLARGE);
        VEHICLE_SLOT_MAP.put(VehicleType.ELECTRIC_VEHICLE, SlotSize.MEDIUM);
    }

    static final double PEAK_HOUR_MULTIPLIER = 1.5;
    static final int PEAK_START_HOUR = 8;  // 8 AM
    static final int PEAK_END_HOUR = 20;   // 8 PM
    static final double VIP_PREMIUM = 1.25; // 25% premium for VIP slots
    static final double LOST_TICKET_PENALTY = 500.0;
    static final double OVERNIGHT_SURCHARGE = 200.0; // per night
    static final double EV_CHARGING_RATE_PER_HOUR = 30.0;
    static final double MINIMUM_FEE = 10.0; // minimum parking fee

    // ========================== SYSTEM STATE ==========================

    static List<ParkingSlot> parkingSlots = new ArrayList<>();
    static Map<String, ParkingTicket> activeTickets = new LinkedHashMap<>(); // ticketId → ticket
    static Map<String, String> vehicleTicketMap = new LinkedHashMap<>(); // licensePlate → ticketId
    static int ticketCounter = 0;

    // ========================== INITIALIZATION ==========================

    public static void initializeParkingLot() {
        parkingSlots.clear();
        activeTickets.clear();
        vehicleTicketMap.clear();
        ticketCounter = 0;

        // Small slots (Bikes) — 10 slots
        for (int i = 1; i <= 10; i++) {
            parkingSlots.add(new ParkingSlot("S" + String.format("%02d", i), SlotSize.SMALL, false, false));
        }

        // Medium slots (Car/EV) — 20 slots, 3 VIP, 5 with EV charger
        for (int i = 1; i <= 20; i++) {
            boolean isVIP = (i <= 3);
            boolean hasEV = (i >= 18);
            parkingSlots.add(new ParkingSlot("M" + String.format("%02d", i), SlotSize.MEDIUM, isVIP, hasEV));
        }

        // Large slots (SUV) — 10 slots, 2 VIP
        for (int i = 1; i <= 10; i++) {
            boolean isVIP = (i <= 2);
            parkingSlots.add(new ParkingSlot("L" + String.format("%02d", i), SlotSize.LARGE, isVIP, false));
        }

        // XLarge slots (Truck) — 5 slots
        for (int i = 1; i <= 5; i++) {
            parkingSlots.add(new ParkingSlot("XL" + String.format("%02d", i), SlotSize.XLARGE, false, false));
        }
    }

    // ========================== CORE LOGIC ==========================

    /**
     * Finds and allocates a suitable parking slot for the vehicle.
     */
    static ParkingSlot findSlot(Vehicle vehicle) {
        SlotSize requiredSize = VEHICLE_SLOT_MAP.get(vehicle.type);
        boolean needsEV = (vehicle.type == VehicleType.ELECTRIC_VEHICLE);

        // For VIP vehicles, try VIP slots first
        if (vehicle.isVIP) {
            for (ParkingSlot slot : parkingSlots) {
                if (!slot.isOccupied && slot.size == requiredSize && slot.isVIP) {
                    if (!needsEV || slot.hasEVCharger) {
                        return slot;
                    }
                }
            }
        }

        // For EV, prefer slots with charger
        if (needsEV) {
            for (ParkingSlot slot : parkingSlots) {
                if (!slot.isOccupied && slot.size == requiredSize && slot.hasEVCharger) {
                    return slot;
                }
            }
        }

        // Any available slot of the right size
        for (ParkingSlot slot : parkingSlots) {
            if (!slot.isOccupied && slot.size == requiredSize) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Registers a vehicle entry.
     */
    public static ParkingResult vehicleEntry(Vehicle vehicle, LocalDateTime entryTime) {
        if (vehicle == null || vehicle.licensePlate == null || vehicle.licensePlate.isEmpty()) {
            return new ParkingResult(false, "Invalid vehicle details.");
        }

        // Check for duplicate
        if (vehicleTicketMap.containsKey(vehicle.licensePlate)) {
            return new ParkingResult(false,
                    "Vehicle " + vehicle.licensePlate + " is already parked.");
        }

        // Find slot
        ParkingSlot slot = findSlot(vehicle);
        if (slot == null) {
            return new ParkingResult(false,
                    "No available " + VEHICLE_SLOT_MAP.get(vehicle.type) + " slot for "
                            + vehicle.type + ".");
        }

        // Create ticket
        ticketCounter++;
        String ticketId = "TK" + String.format("%04d", ticketCounter);
        boolean useEVCharger = (vehicle.type == VehicleType.ELECTRIC_VEHICLE && slot.hasEVCharger);
        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle.licensePlate, vehicle.type,
                slot.slotId, entryTime, vehicle.isVIP, useEVCharger);

        // Update state
        slot.isOccupied = true;
        activeTickets.put(ticketId, ticket);
        vehicleTicketMap.put(vehicle.licensePlate, ticketId);

        String msg = "Vehicle " + vehicle.licensePlate + " parked at slot " + slot.slotId
                + " | Ticket: " + ticketId;
        if (useEVCharger) msg += " | EV charging enabled";
        return new ParkingResult(true, msg, ticket);
    }

    /**
     * Calculates the parking fee.
     */
    public static double calculateFee(ParkingTicket ticket, LocalDateTime exitTime) {
        if (ticket == null || exitTime == null) return 0;

        Duration duration = Duration.between(ticket.entryTime, exitTime);
        long totalMinutes = duration.toMinutes();

        // Early exit (< 15 minutes) = minimum fee
        if (totalMinutes <= 15) {
            return MINIMUM_FEE;
        }

        double totalHours = Math.ceil(totalMinutes / 60.0);
        double baseRate = HOURLY_RATE.getOrDefault(ticket.vehicleType, 40.0);
        double fee = baseRate * totalHours;

        // Peak-hour pricing: check if entry was during peak hours
        int entryHour = ticket.entryTime.getHour();
        if (entryHour >= PEAK_START_HOUR && entryHour < PEAK_END_HOUR) {
            fee *= PEAK_HOUR_MULTIPLIER;
        }

        // VIP premium
        if (ticket.isVIP) {
            fee *= VIP_PREMIUM;
        }

        // Overnight surcharge
        long nights = duration.toDays();
        if (totalMinutes > 0 && nights > 0) {
            fee += nights * OVERNIGHT_SURCHARGE;
        }

        // EV charging fee
        if (ticket.useEVCharger) {
            fee += EV_CHARGING_RATE_PER_HOUR * totalHours;
        }

        // Lost ticket penalty
        if (ticket.isLost) {
            fee += LOST_TICKET_PENALTY;
        }

        return Math.round(fee * 100.0) / 100.0;
    }

    /**
     * Processes a vehicle exit.
     */
    public static ParkingResult vehicleExit(String ticketId, LocalDateTime exitTime) {
        ParkingTicket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            return new ParkingResult(false, "Ticket not found: " + ticketId);
        }

        if (exitTime.isBefore(ticket.entryTime)) {
            return new ParkingResult(false, "Exit time cannot be before entry time.");
        }

        ticket.exitTime = exitTime;
        double fee = calculateFee(ticket, exitTime);

        // Free the slot
        for (ParkingSlot slot : parkingSlots) {
            if (slot.slotId.equals(ticket.slotId)) {
                slot.isOccupied = false;
                break;
            }
        }

        // Remove from active
        activeTickets.remove(ticketId);
        vehicleTicketMap.remove(ticket.licensePlate);

        Duration duration = Duration.between(ticket.entryTime, exitTime);
        ParkingResult result = new ParkingResult(true,
                "Vehicle " + ticket.licensePlate + " exited. Duration: "
                        + duration.toHours() + "h " + (duration.toMinutes() % 60) + "m"
                        + " | Fee: ₹" + fee);
        result.fee = fee;
        return result;
    }

    /**
     * Handles lost ticket — marks ticket with penalty and processes exit.
     */
    public static ParkingResult handleLostTicket(String licensePlate, LocalDateTime exitTime) {
        String ticketId = vehicleTicketMap.get(licensePlate);
        if (ticketId == null) {
            return new ParkingResult(false,
                    "No active parking record for vehicle: " + licensePlate);
        }
        ParkingTicket ticket = activeTickets.get(ticketId);
        ticket.isLost = true;

        ParkingResult result = vehicleExit(ticketId, exitTime);
        if (result.success) {
            result.message += " | Lost ticket penalty: ₹" + LOST_TICKET_PENALTY;
        }
        return result;
    }

    /**
     * Gets the count of available slots by size.
     */
    public static Map<SlotSize, Integer> getAvailability() {
        Map<SlotSize, Integer> availability = new EnumMap<>(SlotSize.class);
        for (SlotSize s : SlotSize.values()) availability.put(s, 0);
        for (ParkingSlot slot : parkingSlots) {
            if (!slot.isOccupied) {
                availability.merge(slot.size, 1, Integer::sum);
            }
        }
        return availability;
    }

    // ========================== MAIN ==========================

    public static void main(String[] args) {
        System.out.println("=== Smart Parking Management System ===\n");

        initializeParkingLot();

        // Show availability
        System.out.println("Initial Availability: " + getAvailability());

        // Park a car
        Vehicle car = new Vehicle("KA01AB1234", VehicleType.CAR, false);
        LocalDateTime entry1 = LocalDateTime.of(2026, 8, 20, 10, 0);
        ParkingResult r1 = vehicleEntry(car, entry1);
        System.out.println(r1.message);

        // Park a VIP SUV
        Vehicle suv = new Vehicle("MH02CD5678", VehicleType.SUV, true);
        ParkingResult r2 = vehicleEntry(suv, entry1);
        System.out.println(r2.message);

        // Park an EV
        Vehicle ev = new Vehicle("DL03EV9999", VehicleType.ELECTRIC_VEHICLE, false);
        ParkingResult r3 = vehicleEntry(ev, entry1);
        System.out.println(r3.message);

        // Exit the car after 3 hours
        LocalDateTime exit1 = entry1.plusHours(3);
        ParkingResult exit = vehicleExit(r1.ticket.ticketId, exit1);
        System.out.println("\n" + exit.message);

        // Lost ticket for SUV
        LocalDateTime exit2 = entry1.plusHours(5);
        ParkingResult lost = handleLostTicket("MH02CD5678", exit2);
        System.out.println(lost.message);

        System.out.println("\nFinal Availability: " + getAvailability());
    }
}
