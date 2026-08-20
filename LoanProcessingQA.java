public class LoanProcessingQA {

    static int passed = 0;
    static int failed = 0;

    // Test approval/rejection result
    static void check(String testName, boolean condition) {

        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            failed++;
        }
    }

    // Test exception
    static void checkException(String testName, Runnable test) {

        try {
            test.run();

            System.out.println("[FAIL] " + testName +
                    " - Exception was not thrown");

            failed++;

        } catch (IllegalArgumentException e) {

            System.out.println("[PASS] " + testName +
                    " - Exception handled");

            passed++;
        }
    }

    // Calculate expected EMI independently
    static double calculateExpectedEMI(
            double principal,
            double annualRate,
            int years) {

        double monthlyRate = annualRate / 12 / 100;
        int months = years * 12;

        return (principal * monthlyRate *
                Math.pow(1 + monthlyRate, months))
                /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("     LOAN PROCESSING QA TEST SUITE");
        System.out.println("======================================\n");

        // ------------------------------------------------
        // 1. Minimum Age Test
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            101,
                            18,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            100000,
                            5
                    );

            check("Minimum age = 18", result != null);

        } catch (Exception e) {
            System.out.println("[FAIL] Minimum age = 18");
            failed++;
        }

        // ------------------------------------------------
        // 2. Maximum Age Test
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            102,
                            65,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            100000,
                            5
                    );

            check("Maximum age = 65", result != null);

        } catch (Exception e) {
            System.out.println("[FAIL] Maximum age = 65");
            failed++;
        }

        // ------------------------------------------------
        // 3. Age Below Minimum
        // ------------------------------------------------

        checkException(
                "Age below minimum",
                () -> LoanProcessingSystem.processLoan(
                        103,
                        17,
                        50000,
                        5000,
                        750,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 4. Age Above Maximum
        // ------------------------------------------------

        checkException(
                "Age above maximum",
                () -> LoanProcessingSystem.processLoan(
                        104,
                        66,
                        50000,
                        5000,
                        750,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 5. Invalid Salary
        // ------------------------------------------------

        checkException(
                "Invalid salary = 0",
                () -> LoanProcessingSystem.processLoan(
                        105,
                        25,
                        0,
                        5000,
                        750,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 6. Negative Salary
        // ------------------------------------------------

        checkException(
                "Negative salary",
                () -> LoanProcessingSystem.processLoan(
                        106,
                        25,
                        -1000,
                        5000,
                        750,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 7. Poor Credit Score
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            107,
                            30,
                            50000,
                            5000,
                            500,
                            "Salaried",
                            100000,
                            5
                    );

            check("Poor credit score rejected",
                    !result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] Poor credit score test");
            failed++;
        }

        // ------------------------------------------------
        // 8. Credit Score Boundary
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            108,
                            30,
                            50000,
                            5000,
                            650,
                            "Salaried",
                            100000,
                            5
                    );

            check("Credit score boundary = 650",
                    result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] Credit score boundary");
            failed++;
        }

        // ------------------------------------------------
        // 9. Existing Loan Exceeding Threshold
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            109,
                            30,
                            50000,
                            600000,
                            750,
                            "Salaried",
                            100000,
                            5
                    );

            check("Existing loan exceeding threshold rejected",
                    !result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] Existing loan threshold");
            failed++;
        }

        // ------------------------------------------------
        // 10. High DTI
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            110,
                            30,
                            50000,
                            25000,
                            750,
                            "Salaried",
                            100000,
                            5
                    );

            check("High DTI rejected",
                    result.dti > 40 && !result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] High DTI test");
            failed++;
        }

        // ------------------------------------------------
        // 11. Salaried Employment
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            111,
                            30,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            200000,
                            5
                    );

            check("Salaried employment",
                    result.eligibleLoanAmount == 250000);

        } catch (Exception e) {
            System.out.println("[FAIL] Salaried employment");
            failed++;
        }

        // ------------------------------------------------
        // 12. Self-Employed
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            112,
                            30,
                            50000,
                            5000,
                            750,
                            "Self-Employed",
                            100000,
                            5
                    );

            check("Self-employed category",
                    result.eligibleLoanAmount == 200000);

        } catch (Exception e) {
            System.out.println("[FAIL] Self-employed category");
            failed++;
        }

        // ------------------------------------------------
        // 13. Business Employment
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            113,
                            30,
                            50000,
                            5000,
                            750,
                            "Business",
                            100000,
                            5
                    );

            check("Business category",
                    result.eligibleLoanAmount == 225000);

        } catch (Exception e) {
            System.out.println("[FAIL] Business category");
            failed++;
        }

        // ------------------------------------------------
        // 14. Contract Employment
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            114,
                            30,
                            50000,
                            5000,
                            750,
                            "Contract",
                            100000,
                            5
                    );

            check("Contract category",
                    result.eligibleLoanAmount == 150000);

        } catch (Exception e) {
            System.out.println("[FAIL] Contract category");
            failed++;
        }

        // ------------------------------------------------
        // 15. Boundary Loan Amount
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            115,
                            30,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            250000,
                            5
                    );

            check("Loan amount exactly at eligible boundary",
                    result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] Boundary loan amount");
            failed++;
        }

        // ------------------------------------------------
        // 16. Loan Amount Above Eligibility
        // ------------------------------------------------

        try {
            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            116,
                            30,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            250001,
                            5
                    );

            check("Loan amount above eligible limit rejected",
                    !result.approved);

        } catch (Exception e) {
            System.out.println("[FAIL] Loan boundary test");
            failed++;
        }

        // ------------------------------------------------
        // 17. EMI Calculation Accuracy
        // ------------------------------------------------

        try {

            double principal = 100000;
            int years = 5;

            LoanProcessingSystem.LoanResult result =
                    LoanProcessingSystem.processLoan(
                            117,
                            30,
                            50000,
                            5000,
                            750,
                            "Salaried",
                            principal,
                            years
                    );

            double expectedEMI =
                    calculateExpectedEMI(
                            principal,
                            7.5,
                            years
                    );

            double difference =
                    Math.abs(result.emi - expectedEMI);

            check("EMI calculation accuracy",
                    difference < 0.01);

        } catch (Exception e) {
            System.out.println("[FAIL] EMI calculation");
            failed++;
        }

        // ------------------------------------------------
        // 18. Negative Existing Loan
        // ------------------------------------------------

        checkException(
                "Negative existing loan",
                () -> LoanProcessingSystem.processLoan(
                        118,
                        30,
                        50000,
                        -5000,
                        750,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 19. Invalid Credit Score
        // ------------------------------------------------

        checkException(
                "Credit score below 300",
                () -> LoanProcessingSystem.processLoan(
                        119,
                        30,
                        50000,
                        5000,
                        200,
                        "Salaried",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // 20. Invalid Requested Loan Amount
        // ------------------------------------------------

        checkException(
                "Invalid requested loan amount",
                () -> LoanProcessingSystem.processLoan(
                        120,
                        30,
                        50000,
                        5000,
                        750,
                        "Salaried",
                        0,
                        5
                )
        );

        // ------------------------------------------------
        // 21. Invalid Loan Tenure
        // ------------------------------------------------

        checkException(
                "Invalid loan tenure",
                () -> LoanProcessingSystem.processLoan(
                        121,
                        30,
                        50000,
                        5000,
                        750,
                        "Salaried",
                        100000,
                        0
                )
        );

        // ------------------------------------------------
        // 22. Empty Employment Type
        // ------------------------------------------------

        checkException(
                "Empty employment type",
                () -> LoanProcessingSystem.processLoan(
                        122,
                        30,
                        50000,
                        5000,
                        750,
                        "",
                        100000,
                        5
                )
        );

        // ------------------------------------------------
        // Final Results
        // ------------------------------------------------

        System.out.println("\n======================================");
        System.out.println("             TEST SUMMARY");
        System.out.println("======================================");

        System.out.println("Tests Passed : " + passed);
        System.out.println("Tests Failed : " + failed);
        System.out.println("Total Tests  : " + (passed + failed));

        if (failed == 0) {
            System.out.println("\nALL TESTS PASSED!");
        } else {
            System.out.println("\nSOME TESTS FAILED!");
        }
    }
}