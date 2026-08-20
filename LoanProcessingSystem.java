import java.util.Scanner;

public class LoanProcessingSystem {

    // Result class to store loan processing details
    static class LoanResult {
        double dti;
        double eligibleLoanAmount;
        double interestRate;
        double emi;
        boolean approved;
        String message;

        LoanResult(double dti, double eligibleLoanAmount,
                   double interestRate, double emi,
                   boolean approved, String message) {
            this.dti = dti;
            this.eligibleLoanAmount = eligibleLoanAmount;
            this.interestRate = interestRate;
            this.emi = emi;
            this.approved = approved;
            this.message = message;
        }
    }

    // Main loan processing method
    public static LoanResult processLoan(
            int customerId,
            int age,
            double monthlySalary,
            double existingLoanAmount,
            int creditScore,
            String employmentType,
            double requestedLoanAmount,
            int loanTenure) {

        // Input validation
        if (age < 18 || age > 65) {
            throw new IllegalArgumentException("Age must be between 18 and 65.");
        }

        if (monthlySalary <= 0) {
            throw new IllegalArgumentException("Salary must be greater than 0.");
        }

        if (existingLoanAmount < 0) {
            throw new IllegalArgumentException("Existing loan amount cannot be negative.");
        }

        if (creditScore < 300 || creditScore > 900) {
            throw new IllegalArgumentException("Credit score must be between 300 and 900.");
        }

        if (requestedLoanAmount <= 0) {
            throw new IllegalArgumentException("Requested loan amount must be greater than 0.");
        }

        if (loanTenure <= 0) {
            throw new IllegalArgumentException("Loan tenure must be greater than 0.");
        }

        if (employmentType == null || employmentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Employment type cannot be empty.");
        }

        // Debt-to-income ratio
        // Assuming existing loan amount is monthly debt for this system
        double dti = (existingLoanAmount / monthlySalary) * 100;

        // Determine interest rate
        double interestRate;

        if (creditScore >= 750) {
            interestRate = 7.5;
        } else if (creditScore >= 700) {
            interestRate = 8.5;
        } else if (creditScore >= 650) {
            interestRate = 10.0;
        } else {
            interestRate = 12.5;
        }

        // Employment-based multiplier
        double employmentMultiplier;

        switch (employmentType.toLowerCase()) {
            case "salaried":
                employmentMultiplier = 5.0;
                break;

            case "self-employed":
                employmentMultiplier = 4.0;
                break;

            case "business":
                employmentMultiplier = 4.5;
                break;

            case "contract":
                employmentMultiplier = 3.0;
                break;

            default:
                employmentMultiplier = 2.5;
        }

        // Calculate eligible loan amount
        double eligibleLoanAmount =
                monthlySalary * employmentMultiplier;

        // Existing loan threshold
        boolean existingLoanValid =
                existingLoanAmount <= monthlySalary * 10;

        // DTI threshold
        boolean dtiValid = dti <= 40;

        // Credit score requirement
        boolean creditValid = creditScore >= 650;

        // Requested amount should not exceed eligible amount
        boolean loanAmountValid =
                requestedLoanAmount <= eligibleLoanAmount;

        // Approval decision
        boolean approved =
                creditValid &&
                existingLoanValid &&
                dtiValid &&
                loanAmountValid;

        String message;

        if (approved) {
            message = "Loan Approved";
        } else {
            message = "Loan Rejected";
        }

        // Calculate EMI
        double monthlyRate = interestRate / 12 / 100;
        int numberOfMonths = loanTenure * 12;

        double emi;

        if (monthlyRate == 0) {
            emi = requestedLoanAmount / numberOfMonths;
        } else {
            emi = (requestedLoanAmount * monthlyRate *
                    Math.pow(1 + monthlyRate, numberOfMonths))
                    /
                    (Math.pow(1 + monthlyRate, numberOfMonths) - 1);
        }

        return new LoanResult(
                dti,
                eligibleLoanAmount,
                interestRate,
                emi,
                approved,
                message
        );
    }

    // Main method
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("===== BANKING LOAN APPROVAL SYSTEM =====");

            System.out.print("Customer ID: ");
            int customerId = scanner.nextInt();

            System.out.print("Age: ");
            int age = scanner.nextInt();

            System.out.print("Monthly Salary: ");
            double salary = scanner.nextDouble();

            System.out.print("Existing Loan Amount: ");
            double existingLoan = scanner.nextDouble();

            System.out.print("Credit Score: ");
            int creditScore = scanner.nextInt();

            scanner.nextLine();

            System.out.print("Employment Type: ");
            String employmentType = scanner.nextLine();

            System.out.print("Requested Loan Amount: ");
            double requestedAmount = scanner.nextDouble();

            System.out.print("Loan Tenure (years): ");
            int tenure = scanner.nextInt();

            LoanResult result = processLoan(
                    customerId,
                    age,
                    salary,
                    existingLoan,
                    creditScore,
                    employmentType,
                    requestedAmount,
                    tenure
            );

            System.out.println("\n===== LOAN RESULT =====");

            System.out.println("Customer ID: " + customerId);
            System.out.printf("Debt-to-Income Ratio: %.2f%%%n", result.dti);
            System.out.printf("Eligible Loan Amount: %.2f%n",
                    result.eligibleLoanAmount);
            System.out.printf("Interest Rate: %.2f%%%n",
                    result.interestRate);
            System.out.printf("Monthly EMI: %.2f%n",
                    result.emi);
            System.out.println("Status: " + result.message);

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        scanner.close();
    }
}