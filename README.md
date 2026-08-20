# Java Management Systems

A collection of 4 Java-based management systems with comprehensive QA test suites.

## Projects

### 1. E-Commerce Order Processing System
- **Development**: `OrderManagement.java`
- **QA Tests**: `OrderManagementQA.java` (25 test scenarios, 38 assertions)
- **Features**: Product ordering, category-specific discounts, coupon validation, GST, free shipping, bulk discounts, max discount caps, out-of-stock handling

### 2. Hospital Appointment and Billing System
- **Development**: `HospitalManagement.java`
- **QA Tests**: `HospitalManagementQA.java` (22 test scenarios, 38 assertions)
- **Features**: Consultation fee (slot-based), lab charges, medicine charges, insurance coverage, emergency surcharge, senior citizen discount, follow-up discount

### 3. Airline Reservation System
- **Development**: `AirlineReservation.java`
- **QA Tests**: `AirlineReservationQA.java` (25 test scenarios, 34 assertions)
- **Features**: Flight search, dynamic pricing, seat availability, booking/cancellation, refund tiers, baggage charges, passenger type discounts

### 4. Smart Parking Management System
- **Development**: `ParkingManagement.java`
- **QA Tests**: `ParkingQA.java` (25 test scenarios, 35 assertions)
- **Features**: Slot allocation by vehicle type, peak-hour pricing, VIP parking, EV charging, lost-ticket penalty, overnight surcharge

## How to Compile and Run

```bash
# Compile all files
javac *.java

# Run individual programs
java OrderManagement
java HospitalManagement
java AirlineReservation
java ParkingManagement

# Run QA test suites
java OrderManagementQA
java HospitalManagementQA
java AirlineReservationQA
java ParkingQA
```

## Requirements
- Java 11 or later
