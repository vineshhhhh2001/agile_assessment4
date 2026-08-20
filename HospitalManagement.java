import java.util.*;

/**
 * Hospital Appointment and Billing System
 *
 * Accepts patient details, doctor, department, appointment type,
 * consultation duration, lab tests, medicines, and insurance information.
 *
 * Calculates:
 *   - Consultation fee
 *   - Lab charges
 *   - Medicine charges
 *   - Insurance coverage
 *   - Patient payable amount
 *
 * Implements rules for:
 *   - Emergency patients
 *   - Senior citizens
 *   - Insurance patients
 *   - Follow-up consultations
 */
public class HospitalManagement {

    // ========================== ENUMS & CONSTANTS ==========================

    enum Department {
        GENERAL, CARDIOLOGY, ORTHOPEDICS, NEUROLOGY, DERMATOLOGY, PEDIATRICS, ENT, ONCOLOGY
    }

    enum AppointmentType {
        REGULAR, EMERGENCY, FOLLOW_UP
    }

    // Base consultation fee per department (per 15 min)
    static final Map<Department, Double> DEPT_BASE_FEE = new EnumMap<>(Department.class);
    static {
        DEPT_BASE_FEE.put(Department.GENERAL, 300.0);
        DEPT_BASE_FEE.put(Department.CARDIOLOGY, 800.0);
        DEPT_BASE_FEE.put(Department.ORTHOPEDICS, 600.0);
        DEPT_BASE_FEE.put(Department.NEUROLOGY, 900.0);
        DEPT_BASE_FEE.put(Department.DERMATOLOGY, 500.0);
        DEPT_BASE_FEE.put(Department.PEDIATRICS, 400.0);
        DEPT_BASE_FEE.put(Department.ENT, 450.0);
        DEPT_BASE_FEE.put(Department.ONCOLOGY, 1200.0);
    }

    // Lab test catalog: name → price
    static final Map<String, Double> LAB_TESTS = new LinkedHashMap<>();
    static {
        LAB_TESTS.put("Blood Test", 500.0);
        LAB_TESTS.put("Urine Test", 300.0);
        LAB_TESTS.put("X-Ray", 1200.0);
        LAB_TESTS.put("MRI", 8000.0);
        LAB_TESTS.put("CT Scan", 5000.0);
        LAB_TESTS.put("ECG", 1500.0);
        LAB_TESTS.put("Ultrasound", 2000.0);
        LAB_TESTS.put("Thyroid Panel", 800.0);
        LAB_TESTS.put("Liver Function Test", 700.0);
        LAB_TESTS.put("COVID RT-PCR", 500.0);
    }

    // Medicine catalog: name → price
    static final Map<String, Double> MEDICINES = new LinkedHashMap<>();
    static {
        MEDICINES.put("Paracetamol", 30.0);
        MEDICINES.put("Amoxicillin", 120.0);
        MEDICINES.put("Ibuprofen", 50.0);
        MEDICINES.put("Omeprazole", 80.0);
        MEDICINES.put("Metformin", 100.0);
        MEDICINES.put("Amlodipine", 150.0);
        MEDICINES.put("Atorvastatin", 200.0);
        MEDICINES.put("Insulin Injection", 1500.0);
        MEDICINES.put("Cough Syrup", 90.0);
        MEDICINES.put("Vitamin D Supplement", 250.0);
    }

    static final double EMERGENCY_SURCHARGE = 1.5;     // 1.5x normal consultation
    static final double FOLLOW_UP_DISCOUNT = 0.5;       // 50% off consultation
    static final double SENIOR_CITIZEN_AGE = 60;
    static final double SENIOR_CITIZEN_DISCOUNT = 0.10;  // 10% overall discount
    static final double MAX_INSURANCE_COVERAGE = 0.80;   // Max 80% coverage

    // ========================== DATA MODELS ==========================

    static class Patient {
        String patientId;
        String name;
        int age;
        String gender;
        boolean hasInsurance;
        String insuranceProvider;
        double insuranceCoveragePercent; // e.g., 70 for 70%

        public Patient(String patientId, String name, int age, String gender,
                       boolean hasInsurance, String insuranceProvider, double insuranceCoveragePercent) {
            this.patientId = patientId;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.hasInsurance = hasInsurance;
            this.insuranceProvider = insuranceProvider;
            this.insuranceCoveragePercent = insuranceCoveragePercent;
        }
    }

    static class Appointment {
        Patient patient;
        String doctorName;
        Department department;
        AppointmentType type;
        int consultationMinutes; // duration
        List<String> labTests;  // test names
        Map<String, Integer> medicines; // medicine name → quantity

        public Appointment(Patient patient, String doctorName, Department department,
                           AppointmentType type, int consultationMinutes,
                           List<String> labTests, Map<String, Integer> medicines) {
            this.patient = patient;
            this.doctorName = doctorName;
            this.department = department;
            this.type = type;
            this.consultationMinutes = consultationMinutes;
            this.labTests = labTests != null ? labTests : new ArrayList<>();
            this.medicines = medicines != null ? medicines : new HashMap<>();
        }
    }

    static class HospitalBill {
        double consultationFee;
        double labCharges;
        double medicineCharges;
        double grossTotal;
        double seniorCitizenDiscount;
        double insuranceCoverage;
        double patientPayable;
        List<String> notes;
        List<String> errors;

        public HospitalBill() {
            notes = new ArrayList<>();
            errors = new ArrayList<>();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("============ HOSPITAL BILL ============\n");
            sb.append(String.format("Consultation Fee:     ₹%.2f%n", consultationFee));
            sb.append(String.format("Lab Charges:          ₹%.2f%n", labCharges));
            sb.append(String.format("Medicine Charges:     ₹%.2f%n", medicineCharges));
            sb.append(String.format("Gross Total:          ₹%.2f%n", grossTotal));
            sb.append(String.format("Senior Discount:     -₹%.2f%n", seniorCitizenDiscount));
            sb.append(String.format("Insurance Coverage:  -₹%.2f%n", insuranceCoverage));
            sb.append(String.format("Patient Payable:      ₹%.2f%n", patientPayable));
            if (!notes.isEmpty()) {
                sb.append("--- Notes ---\n");
                for (String n : notes) sb.append("  • ").append(n).append("\n");
            }
            if (!errors.isEmpty()) {
                sb.append("--- Errors ---\n");
                for (String e : errors) sb.append("  ✗ ").append(e).append("\n");
            }
            sb.append("=======================================\n");
            return sb.toString();
        }
    }

    // ========================== CORE LOGIC ==========================

    /**
     * Generates a hospital bill for the given appointment.
     */
    public static HospitalBill generateBill(Appointment appointment) {
        HospitalBill bill = new HospitalBill();

        if (appointment == null) {
            bill.errors.add("No appointment provided.");
            return bill;
        }

        Patient patient = appointment.patient;
        if (patient == null) {
            bill.errors.add("No patient information provided.");
            return bill;
        }

        if (appointment.consultationMinutes <= 0) {
            bill.errors.add("Invalid consultation duration: " + appointment.consultationMinutes + " minutes.");
            return bill;
        }

        // ---------- Consultation Fee ----------
        double baseFee = DEPT_BASE_FEE.getOrDefault(appointment.department, 300.0);
        int slots = (int) Math.ceil(appointment.consultationMinutes / 15.0);
        double consultationFee = baseFee * slots;

        // Apply appointment type modifier
        switch (appointment.type) {
            case EMERGENCY:
                consultationFee *= EMERGENCY_SURCHARGE;
                bill.notes.add("Emergency surcharge applied (1.5x).");
                break;
            case FOLLOW_UP:
                consultationFee *= FOLLOW_UP_DISCOUNT;
                bill.notes.add("Follow-up discount applied (50% off consultation).");
                break;
            case REGULAR:
            default:
                break;
        }
        bill.consultationFee = Math.round(consultationFee * 100.0) / 100.0;

        // ---------- Lab Charges ----------
        double labCharges = 0;
        for (String testName : appointment.labTests) {
            Double price = LAB_TESTS.get(testName);
            if (price != null) {
                labCharges += price;
            } else {
                bill.errors.add("Unknown lab test: " + testName);
            }
        }
        bill.labCharges = Math.round(labCharges * 100.0) / 100.0;

        // ---------- Medicine Charges ----------
        double medicineCharges = 0;
        for (Map.Entry<String, Integer> entry : appointment.medicines.entrySet()) {
            String medName = entry.getKey();
            int qty = entry.getValue();
            Double price = MEDICINES.get(medName);
            if (price != null) {
                if (qty <= 0) {
                    bill.errors.add("Invalid quantity for medicine " + medName + ": " + qty);
                } else {
                    medicineCharges += price * qty;
                }
            } else {
                bill.errors.add("Unknown medicine: " + medName);
            }
        }
        bill.medicineCharges = Math.round(medicineCharges * 100.0) / 100.0;

        // ---------- Gross Total ----------
        double grossTotal = bill.consultationFee + bill.labCharges + bill.medicineCharges;
        bill.grossTotal = Math.round(grossTotal * 100.0) / 100.0;

        // ---------- Senior Citizen Discount ----------
        double seniorDiscount = 0;
        if (patient.age >= SENIOR_CITIZEN_AGE) {
            seniorDiscount = grossTotal * SENIOR_CITIZEN_DISCOUNT;
            bill.notes.add("Senior citizen discount applied (10%).");
        }
        bill.seniorCitizenDiscount = Math.round(seniorDiscount * 100.0) / 100.0;

        double afterSenior = grossTotal - seniorDiscount;

        // ---------- Insurance Coverage ----------
        double insuranceCoverage = 0;
        if (patient.hasInsurance) {
            double coveragePercent = patient.insuranceCoveragePercent / 100.0;
            if (coveragePercent > MAX_INSURANCE_COVERAGE) {
                coveragePercent = MAX_INSURANCE_COVERAGE;
                bill.notes.add("Insurance coverage capped at 80%.");
            }
            insuranceCoverage = afterSenior * coveragePercent;
            bill.notes.add("Insurance (" + patient.insuranceProvider + ") coverage: "
                    + (coveragePercent * 100) + "%");
        }
        bill.insuranceCoverage = Math.round(insuranceCoverage * 100.0) / 100.0;

        // ---------- Patient Payable ----------
        double payable = afterSenior - insuranceCoverage;
        if (payable < 0) payable = 0;
        bill.patientPayable = Math.round(payable * 100.0) / 100.0;

        return bill;
    }

    // ========================== MAIN ==========================

    public static void main(String[] args) {
        System.out.println("=== Hospital Appointment & Billing System ===\n");

        // Regular patient
        Patient p1 = new Patient("PAT001", "Rahul Sharma", 35, "Male",
                false, null, 0);
        Appointment a1 = new Appointment(p1, "Dr. Mehta", Department.CARDIOLOGY,
                AppointmentType.REGULAR, 30,
                Arrays.asList("Blood Test", "ECG"),
                Map.of("Atorvastatin", 2, "Amlodipine", 1));
        System.out.println(generateBill(a1));

        // Emergency senior citizen with insurance
        Patient p2 = new Patient("PAT002", "Kamala Devi", 72, "Female",
                true, "Star Health", 70);
        Appointment a2 = new Appointment(p2, "Dr. Rao", Department.NEUROLOGY,
                AppointmentType.EMERGENCY, 45,
                Arrays.asList("MRI", "Blood Test"),
                Map.of("Paracetamol", 3, "Ibuprofen", 2));
        System.out.println(generateBill(a2));

        // Follow-up consultation
        Patient p3 = new Patient("PAT003", "Anil Kumar", 45, "Male",
                true, "ICICI Lombard", 50);
        Appointment a3 = new Appointment(p3, "Dr. Singh", Department.ORTHOPEDICS,
                AppointmentType.FOLLOW_UP, 15,
                new ArrayList<>(),
                Map.of("Ibuprofen", 1));
        System.out.println(generateBill(a3));
    }
}
