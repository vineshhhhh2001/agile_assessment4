import java.util.*;

/**
 * QA Test Suite for Hospital Appointment and Billing System
 * Tests 20+ patient scenarios covering all billing rules and edge cases.
 */
public class HospitalManagementQA {

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

    // Test 1: Regular consultation — General department, 15 min
    static void testRegularGeneral() {
        System.out.println("\nTest 1: Regular General Consultation (15 min)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P1", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. A", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertEqual(300.0, bill.consultationFee, "Consultation fee = 300 (1 slot × 300)");
        assertEqual(300.0, bill.patientPayable, "Patient payable = 300");
    }

    // Test 2: Emergency surcharge
    static void testEmergencySurcharge() {
        System.out.println("\nTest 2: Emergency Surcharge (1.5x)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P2", "Test", 40, "F", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. B", HospitalManagement.Department.CARDIOLOGY,
                HospitalManagement.AppointmentType.EMERGENCY, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // 1 slot × 800 × 1.5 = 1200
        assertEqual(1200.0, bill.consultationFee, "Emergency consultation = 800 × 1.5");
    }

    // Test 3: Follow-up discount
    static void testFollowUpDiscount() {
        System.out.println("\nTest 3: Follow-Up Discount (50% off)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P3", "Test", 25, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. C", HospitalManagement.Department.ORTHOPEDICS,
                HospitalManagement.AppointmentType.FOLLOW_UP, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // 1 slot × 600 × 0.5 = 300
        assertEqual(300.0, bill.consultationFee, "Follow-up = 600 × 0.5");
    }

    // Test 4: Senior citizen discount
    static void testSeniorCitizenDiscount() {
        System.out.println("\nTest 4: Senior Citizen Discount (10%)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P4", "Test", 65, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. D", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertEqual(30.0, bill.seniorCitizenDiscount, "Senior discount = 10% of 300");
        assertEqual(270.0, bill.patientPayable, "Payable after senior discount");
    }

    // Test 5: Insurance coverage
    static void testInsuranceCoverage() {
        System.out.println("\nTest 5: Insurance Coverage (70%)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P5", "Test", 40, "F",
                true, "Star Health", 70);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. E", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // Gross = 300, insurance = 70% of 300 = 210
        assertEqual(210.0, bill.insuranceCoverage, "Insurance covers 70%");
        assertEqual(90.0, bill.patientPayable, "Payable after insurance");
    }

    // Test 6: Insurance capped at 80%
    static void testInsuranceCap() {
        System.out.println("\nTest 6: Insurance Coverage Capped at 80%");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P6", "Test", 35, "M",
                true, "Max Bupa", 95);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. F", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertEqual(240.0, bill.insuranceCoverage, "Insurance capped at 80% of 300");
    }

    // Test 7: Lab charges
    static void testLabCharges() {
        System.out.println("\nTest 7: Lab Charges");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P7", "Test", 30, "F", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. G", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                Arrays.asList("Blood Test", "X-Ray"), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // Blood Test 500 + X-Ray 1200 = 1700
        assertEqual(1700.0, bill.labCharges, "Lab charges = 500 + 1200");
    }

    // Test 8: Medicine charges
    static void testMedicineCharges() {
        System.out.println("\nTest 8: Medicine Charges");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P8", "Test", 30, "M", false, null, 0);
        Map<String, Integer> meds = new HashMap<>();
        meds.put("Paracetamol", 3);
        meds.put("Amoxicillin", 2);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. H", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), meds);
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // Paracetamol 30×3=90, Amoxicillin 120×2=240 → 330
        assertEqual(330.0, bill.medicineCharges, "Medicine = 90 + 240");
    }

    // Test 9: Unknown lab test
    static void testUnknownLabTest() {
        System.out.println("\nTest 9: Unknown Lab Test");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P9", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. I", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                Arrays.asList("Blood Test", "Quantum Scan"), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Unknown lab test")), "Error for unknown test");
        assertEqual(500.0, bill.labCharges, "Only valid test counted");
    }

    // Test 10: Unknown medicine
    static void testUnknownMedicine() {
        System.out.println("\nTest 10: Unknown Medicine");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P10", "Test", 30, "F", false, null, 0);
        Map<String, Integer> meds = new HashMap<>();
        meds.put("MagicPill", 1);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. J", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), meds);
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Unknown medicine")), "Error for unknown medicine");
    }

    // Test 11: Multi-slot consultation (30 min = 2 slots)
    static void testMultiSlotConsultation() {
        System.out.println("\nTest 11: Multi-Slot Consultation (30 min)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P11", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. K", HospitalManagement.Department.NEUROLOGY,
                HospitalManagement.AppointmentType.REGULAR, 30,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // 2 slots × 900 = 1800
        assertEqual(1800.0, bill.consultationFee, "2 slots × 900");
    }

    // Test 12: Invalid consultation duration (0 min)
    static void testZeroDuration() {
        System.out.println("\nTest 12: Zero Consultation Duration");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P12", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. L", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 0,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Invalid consultation duration")), "Error for 0 duration");
    }

    // Test 13: Negative consultation duration
    static void testNegativeDuration() {
        System.out.println("\nTest 13: Negative Consultation Duration");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P13", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. M", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, -10,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Invalid consultation duration")), "Error for negative duration");
    }

    // Test 14: Null appointment
    static void testNullAppointment() {
        System.out.println("\nTest 14: Null Appointment");
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(null);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("No appointment")), "Error for null appointment");
    }

    // Test 15: Null patient
    static void testNullPatient() {
        System.out.println("\nTest 15: Null Patient");
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                null, "Dr. N", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("No patient")), "Error for null patient");
    }

    // Test 16: Emergency senior citizen with insurance
    static void testEmergencySeniorInsured() {
        System.out.println("\nTest 16: Emergency Senior Citizen with Insurance");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P16", "Test", 70, "F",
                true, "Star Health", 60);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. O", HospitalManagement.Department.CARDIOLOGY,
                HospitalManagement.AppointmentType.EMERGENCY, 30,
                Arrays.asList("ECG", "Blood Test"), Map.of("Amlodipine", 1));
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // Consultation: 2 slots × 800 × 1.5 = 2400
        assertEqual(2400.0, bill.consultationFee, "Emergency cardiology 2 slots");
        // Lab: 1500 + 500 = 2000
        assertEqual(2000.0, bill.labCharges, "ECG + Blood Test");
        // Medicine: 150
        assertEqual(150.0, bill.medicineCharges, "Amlodipine × 1");
        // Gross: 2400+2000+150 = 4550
        assertEqual(4550.0, bill.grossTotal, "Gross total");
        // Senior: 10% of 4550 = 455
        assertEqual(455.0, bill.seniorCitizenDiscount, "Senior discount");
        // After senior: 4095, Insurance: 60% of 4095 = 2457
        assertEqual(2457.0, bill.insuranceCoverage, "Insurance 60%");
        assertEqual(1638.0, bill.patientPayable, "Patient payable");
    }

    // Test 17: Full bill — all components
    static void testFullBill() {
        System.out.println("\nTest 17: Full Bill — All Components");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P17", "Test", 45, "M",
                true, "ICICI Lombard", 50);
        Map<String, Integer> meds = new HashMap<>();
        meds.put("Paracetamol", 2);
        meds.put("Cough Syrup", 1);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. P", HospitalManagement.Department.ENT,
                HospitalManagement.AppointmentType.REGULAR, 20,
                Arrays.asList("Blood Test", "Thyroid Panel"), meds);
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // Consultation: 2 slots × 450 = 900 (20 min → ceil(20/15) = 2)
        assertEqual(900.0, bill.consultationFee, "ENT 2 slots");
        // Lab: 500 + 800 = 1300
        assertEqual(1300.0, bill.labCharges, "Blood + Thyroid");
        // Medicine: 30×2 + 90×1 = 150
        assertEqual(150.0, bill.medicineCharges, "Paracetamol×2 + Cough Syrup×1");
        assertTrue(bill.insuranceCoverage > 0, "Insurance applied");
    }

    // Test 18: No lab tests and no medicines
    static void testConsultationOnly() {
        System.out.println("\nTest 18: Consultation Only (no labs/meds)");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P18", "Test", 28, "F", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. Q", HospitalManagement.Department.DERMATOLOGY,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), new HashMap<>());
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertEqual(500.0, bill.consultationFee, "Dermatology base fee");
        assertEqual(0.0, bill.labCharges, "No lab charges");
        assertEqual(0.0, bill.medicineCharges, "No medicine charges");
    }

    // Test 19: Oncology department (high base fee)
    static void testOncology() {
        System.out.println("\nTest 19: Oncology Department");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P19", "Test", 55, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. R", HospitalManagement.Department.ONCOLOGY,
                HospitalManagement.AppointmentType.REGULAR, 45,
                Arrays.asList("CT Scan", "Blood Test"), Map.of("Metformin", 2));
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // 3 slots × 1200 = 3600
        assertEqual(3600.0, bill.consultationFee, "Oncology 3 slots");
    }

    // Test 20: Pediatrics follow-up
    static void testPediatricsFollowUp() {
        System.out.println("\nTest 20: Pediatrics Follow-Up");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P20", "Child", 5, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. S", HospitalManagement.Department.PEDIATRICS,
                HospitalManagement.AppointmentType.FOLLOW_UP, 15,
                new ArrayList<>(), Map.of("Cough Syrup", 1));
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        // 1 slot × 400 × 0.5 = 200
        assertEqual(200.0, bill.consultationFee, "Pediatrics follow-up");
        assertEqual(290.0, bill.patientPayable, "200 + 90 medicine");
    }

    // Test 21: Medicine with invalid quantity
    static void testInvalidMedicineQuantity() {
        System.out.println("\nTest 21: Invalid Medicine Quantity");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P21", "Test", 30, "M", false, null, 0);
        Map<String, Integer> meds = new HashMap<>();
        meds.put("Paracetamol", -1);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. T", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                new ArrayList<>(), meds);
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        assertTrue(bill.errors.stream().anyMatch(e -> e.contains("Invalid quantity")), "Error for invalid med qty");
    }

    // Test 22: Gross total = sum of all charges
    static void testGrossTotal() {
        System.out.println("\nTest 22: Gross Total = Consultation + Lab + Medicine");
        HospitalManagement.Patient p = new HospitalManagement.Patient("P22", "Test", 30, "M", false, null, 0);
        HospitalManagement.Appointment a = new HospitalManagement.Appointment(
                p, "Dr. U", HospitalManagement.Department.GENERAL,
                HospitalManagement.AppointmentType.REGULAR, 15,
                Arrays.asList("Urine Test"), Map.of("Omeprazole", 1));
        HospitalManagement.HospitalBill bill = HospitalManagement.generateBill(a);
        double expectedGross = bill.consultationFee + bill.labCharges + bill.medicineCharges;
        assertEqual(expectedGross, bill.grossTotal, "Gross total formula");
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" HospitalManagement QA Test Suite");
        System.out.println("==============================================");

        testRegularGeneral();
        testEmergencySurcharge();
        testFollowUpDiscount();
        testSeniorCitizenDiscount();
        testInsuranceCoverage();
        testInsuranceCap();
        testLabCharges();
        testMedicineCharges();
        testUnknownLabTest();
        testUnknownMedicine();
        testMultiSlotConsultation();
        testZeroDuration();
        testNegativeDuration();
        testNullAppointment();
        testNullPatient();
        testEmergencySeniorInsured();
        testFullBill();
        testConsultationOnly();
        testOncology();
        testPediatricsFollowUp();
        testInvalidMedicineQuantity();
        testGrossTotal();

        System.out.println("\n==============================================");
        System.out.println(" Results: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println(" Total:   " + (testsPassed + testsFailed) + " tests");
        System.out.println("==============================================");
    }
}
