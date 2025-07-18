import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

// Transaction class to represent a financial transaction
class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    // Getters
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getDescription() { return description; }
    public String getVendor() { return vendor; }
    public double getAmount() { return amount; }

    // Check if transaction is a deposit (positive amount)
    public boolean isDeposit() {
        return amount > 0;
    }

    // Check if transaction is a payment (negative amount)
    public boolean isPayment() {
        return amount < 0;
    }

    // Convert to CSV format for file storage
    public String toCSV() {
        return String.format("%s|%s|%s|%s|%.2f",
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                time.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                description,
                vendor,
                amount);
    }

    // Create Transaction from CSV line
    public static Transaction fromCSV(String csvLine) throws IllegalArgumentException {
        String[] parts = csvLine.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid CSV format: " + csvLine);
        }

        try {
            LocalDate date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime time = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH:mm:ss"));
            String description = parts[2];
            String vendor = parts[3];
            double amount = Double.parseDouble(parts[4]);

            return new Transaction(date, time, description, vendor, amount);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid data format in CSV: " + csvLine);
        }
    }

    // Format for display
    @Override
    public String toString() {
        return String.format("%-12s %-10s %-30s %-20s %10.2f",
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                time.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                description.length() > 30 ? description.substring(0, 27) + "..." : description,
                vendor.length() > 20 ? vendor.substring(0, 17) + "..." : vendor,
                amount);
    }
}

// Custom exception for application-specific errors
class LedgerException extends Exception {
    public LedgerException(String message) {
        super(message);
    }
}

// File manager for CSV operations
class TransactionFileManager {
    private static final String FILENAME = "transactions.csv";
    private static final String HEADER = "date|time|description|vendor|amount";

    public static void saveTransaction(Transaction transaction) throws LedgerException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILENAME, true))) {
            writer.println(transaction.toCSV());
        } catch (IOException e) {
            throw new LedgerException("Error saving transaction: " + e.getMessage());
        }
    }

    public static List<Transaction> loadTransactions() throws LedgerException {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(FILENAME);

        if (!file.exists()) {
            try {
                file.createNewFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(FILENAME))) {
                    writer.println(HEADER);
                }
            } catch (IOException e) {
                throw new LedgerException("Error creating transactions file: " + e.getMessage());
            }
            return transactions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine && line.equals(HEADER)) {
                    firstLine = false;
                    continue;
                }

                try {
                    if (!line.trim().isEmpty()) {
                        transactions.add(Transaction.fromCSV(line));
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Skipping invalid transaction: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new LedgerException("Error loading transactions: " + e.getMessage());
        }

        // Sort by date and time (newest first)
        transactions.sort((t1, t2) -> {
            int dateComparison = t2.getDate().compareTo(t1.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return t2.getTime().compareTo(t1.getTime());
        });

        return transactions;
    }
}

// Input validation utilities
class InputValidator {
    private static final Scanner scanner = new Scanner(System.in);

    public static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static String getStringInput(String prompt, boolean required) {
        String input;
        do {
            input = getStringInput(prompt);
            if (!required || !input.isEmpty()) {
                break;
            }
            System.out.println("This field is required. Please enter a value.");
        } while (true);
        return input;
    }

    public static double getDoubleInput(String prompt) throws NumberFormatException {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            throw new NumberFormatException("Amount cannot be empty");
        }
        return Double.parseDouble(input);
    }

    public static LocalDate getDateInput(String prompt, boolean required) {
        String input;
        do {
            input = getStringInput(prompt);
            if (!required && input.isEmpty()) {
                return null;
            }
            if (input.isEmpty() && required) {
                System.out.println("Date is required. Please enter a valid date (yyyy-MM-dd).");
                continue;
            }

            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd format.");
            }
        } while (true);
    }

    public static char getCharInput(String prompt, char... validChars) {
        String validOptions = Arrays.toString(validChars).replaceAll("[\\[\\]]", "");
        String input;
        do {
            input = getStringInput(prompt).toUpperCase();
            if (input.length() == 1) {
                char choice = input.charAt(0);
                for (char validChar : validChars) {
                    if (choice == Character.toUpperCase(validChar)) {
                        return choice;
                    }
                }
            }
            System.out.println("Please enter one of: " + validOptions);
        } while (true);
    }
}

// Report generator
class ReportGenerator {

    public static List<Transaction> getMonthToDateTransactions(List<Transaction> transactions) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startOfMonth))
                .collect(Collectors.toList());
    }

    public static List<Transaction> getPreviousMonthTransactions(List<Transaction> transactions) {
        LocalDate now = LocalDate.now();
        LocalDate startOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfPreviousMonth = startOfPreviousMonth.plusMonths(1).minusDays(1);

        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startOfPreviousMonth) &&
                        !t.getDate().isAfter(endOfPreviousMonth))
                .collect(Collectors.toList());
    }

    public static List<Transaction> getYearToDateTransactions(List<Transaction> transactions) {
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);

        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startOfYear))
                .collect(Collectors.toList());
    }

    public static List<Transaction> getPreviousYearTransactions(List<Transaction> transactions) {
        LocalDate now = LocalDate.now();
        LocalDate startOfPreviousYear = now.minusYears(1).withDayOfYear(1);
        LocalDate endOfPreviousYear = startOfPreviousYear.plusYears(1).minusDays(1);

        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startOfPreviousYear) &&
                        !t.getDate().isAfter(endOfPreviousYear))
                .collect(Collectors.toList());
    }

    public static List<Transaction> getTransactionsByVendor(List<Transaction> transactions, String vendor) {
        return transactions.stream()
                .filter(t -> t.getVendor().toLowerCase().contains(vendor.toLowerCase()))
                .collect(Collectors.toList());
    }

    public static List<Transaction> getCustomSearchResults(List<Transaction> transactions,
                                                           LocalDate startDate, LocalDate endDate,
                                                           String description, String vendor,
                                                           Double minAmount, Double maxAmount) {
        return transactions.stream()
                .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
                .filter(t -> description == null || description.isEmpty() ||
                        t.getDescription().toLowerCase().contains(description.toLowerCase()))
                .filter(t -> vendor == null || vendor.isEmpty() ||
                        t.getVendor().toLowerCase().contains(vendor.toLowerCase()))
                .filter(t -> minAmount == null || t.getAmount() >= minAmount)
                .filter(t -> maxAmount == null || t.getAmount() <= maxAmount)
                .collect(Collectors.toList());
    }
}

// Main application class
public class AccountingLedgerApp {
    private static List<Transaction> transactions = new ArrayList<>();
    private static final String SEPARATOR = "=".repeat(80);
    private static final String LINE = "-".repeat(80);

    public static void main(String[] args) {
        System.out.println("Welcome to the Enhanced Accounting Ledger Application!");
        System.out.println(SEPARATOR);

        try {
            transactions = TransactionFileManager.loadTransactions();
            System.out.println("✓ Loaded " + transactions.size() + " transactions from file.");
        } catch (LedgerException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            System.out.println("Starting with empty ledger.");
        }

        runApplication();
    }

    private static void runApplication() {
        boolean running = true;
        while (running) {
            try {
                displayHomeScreen();
                char choice = InputValidator.getCharInput("Choose an option: ", 'D', 'P', 'L', 'X');

                switch (choice) {
                    case 'D':
                        addDeposit();
                        break;
                    case 'P':
                        makePayment();
                        break;
                    case 'L':
                        showLedger();
                        break;
                    case 'X':
                        running = false;
                        System.out.println("Thank you for using the Accounting Ledger Application!");
                        break;
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private static void displayHomeScreen() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        HOME SCREEN");
        System.out.println(SEPARATOR);
        System.out.println("D) Add Deposit");
        System.out.println("P) Make Payment (Debit)");
        System.out.println("L) Ledger");
        System.out.println("X) Exit");
        System.out.println(LINE);
    }

    private static void addDeposit() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        ADD DEPOSIT");
        System.out.println(SEPARATOR);

        try {
            String description = InputValidator.getStringInput("Description: ", true);
            String vendor = InputValidator.getStringInput("Vendor: ", true);
            double amount = InputValidator.getDoubleInput("Amount: $");

            if (amount <= 0) {
                System.out.println("Deposit amount must be positive.");
                return;
            }

            Transaction transaction = new Transaction(
                    LocalDate.now(),
                    LocalTime.now(),
                    description,
                    vendor,
                    amount
            );

            TransactionFileManager.saveTransaction(transaction);
            transactions.add(0, transaction); // Add to beginning for newest first

            System.out.println("✓ Deposit added successfully!");
            System.out.println("Transaction: " + transaction);

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (LedgerException e) {
            System.err.println("Error saving deposit: " + e.getMessage());
        }
    }

    private static void makePayment() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        MAKE PAYMENT");
        System.out.println(SEPARATOR);

        try {
            String description = InputValidator.getStringInput("Description: ", true);
            String vendor = InputValidator.getStringInput("Vendor: ", true);
            double amount = InputValidator.getDoubleInput("Amount: $");

            if (amount <= 0) {
                System.out.println("Payment amount must be positive.");
                return;
            }

            Transaction transaction = new Transaction(
                    LocalDate.now(),
                    LocalTime.now(),
                    description,
                    vendor,
                    -amount // Negative for payment
            );

            TransactionFileManager.saveTransaction(transaction);
            transactions.add(0, transaction); // Add to beginning for newest first

            System.out.println("✓ Payment recorded successfully!");
            System.out.println("Transaction: " + transaction);

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (LedgerException e) {
            System.err.println("Error saving payment: " + e.getMessage());
        }
    }

    private static void showLedger() {
        boolean inLedger = true;
        while (inLedger) {
            displayLedgerScreen();
            char choice = InputValidator.getCharInput("Choose an option: ", 'A', 'D', 'P', 'R', 'H');

            switch (choice) {
                case 'A':
                    displayTransactions(transactions, "ALL TRANSACTIONS");
                    break;
                case 'D':
                    List<Transaction> deposits = transactions.stream()
                            .filter(Transaction::isDeposit)
                            .collect(Collectors.toList());
                    displayTransactions(deposits, "DEPOSITS");
                    break;
                case 'P':
                    List<Transaction> payments = transactions.stream()
                            .filter(Transaction::isPayment)
                            .collect(Collectors.toList());
                    displayTransactions(payments, "PAYMENTS");
                    break;
                case 'R':
                    showReports();
                    break;
                case 'H':
                    inLedger = false;
                    break;
            }
        }
    }

    private static void displayLedgerScreen() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        LEDGER");
        System.out.println(SEPARATOR);
        System.out.println("A) All Transactions");
        System.out.println("D) Deposits Only");
        System.out.println("P) Payments Only");
        System.out.println("R) Reports");
        System.out.println("H) Home");
        System.out.println(LINE);
    }

    private static void showReports() {
        boolean inReports = true;
        while (inReports) {
            displayReportsScreen();
            char choice = InputValidator.getCharInput("Choose an option: ",
                    '1', '2', '3', '4', '5', '6', '0');

            switch (choice) {
                case '1':
                    displayTransactions(
                            ReportGenerator.getMonthToDateTransactions(transactions),
                            "MONTH TO DATE"
                    );
                    break;
                case '2':
                    displayTransactions(
                            ReportGenerator.getPreviousMonthTransactions(transactions),
                            "PREVIOUS MONTH"
                    );
                    break;
                case '3':
                    displayTransactions(
                            ReportGenerator.getYearToDateTransactions(transactions),
                            "YEAR TO DATE"
                    );
                    break;
                case '4':
                    displayTransactions(
                            ReportGenerator.getPreviousYearTransactions(transactions),
                            "PREVIOUS YEAR"
                    );
                    break;
                case '5':
                    searchByVendor();
                    break;
                case '6':
                    customSearch();
                    break;
                case '0':
                    inReports = false;
                    break;
            }
        }
    }

    private static void displayReportsScreen() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        REPORTS");
        System.out.println(SEPARATOR);
        System.out.println("1) Month To Date");
        System.out.println("2) Previous Month");
        System.out.println("3) Year To Date");
        System.out.println("4) Previous Year");
        System.out.println("5) Search by Vendor");
        System.out.println("6) Custom Search");
        System.out.println("0) Back to Ledger");
        System.out.println(LINE);
    }

    private static void searchByVendor() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        SEARCH BY VENDOR");
        System.out.println(SEPARATOR);

        String vendor = InputValidator.getStringInput("Enter vendor name: ", true);
        List<Transaction> results = ReportGenerator.getTransactionsByVendor(transactions, vendor);

        displayTransactions(results, "VENDOR SEARCH: " + vendor);
    }

    private static void customSearch() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        CUSTOM SEARCH");
        System.out.println(SEPARATOR);
        System.out.println("Enter search criteria (leave blank to skip):");

        LocalDate startDate = InputValidator.getDateInput("Start Date (yyyy-MM-dd): ", false);
        LocalDate endDate = InputValidator.getDateInput("End Date (yyyy-MM-dd): ", false);
        String description = InputValidator.getStringInput("Description: ", false);
        String vendor = InputValidator.getStringInput("Vendor: ", false);

        Double minAmount = null;
        Double maxAmount = null;

        try {
            String minAmountStr = InputValidator.getStringInput("Minimum Amount: $", false);
            if (!minAmountStr.isEmpty()) {
                minAmount = Double.parseDouble(minAmountStr);
            }

            String maxAmountStr = InputValidator.getStringInput("Maximum Amount: $", false);
            if (!maxAmountStr.isEmpty()) {
                maxAmount = Double.parseDouble(maxAmountStr);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format. Skipping amount filters.");
        }

        List<Transaction> results = ReportGenerator.getCustomSearchResults(
                transactions, startDate, endDate, description, vendor, minAmount, maxAmount
        );

        displayTransactions(results, "CUSTOM SEARCH RESULTS");
    }

    private static void displayTransactions(List<Transaction> transactionList, String title) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("                        " + title);
        System.out.println(SEPARATOR);

        if (transactionList.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        // Header
        System.out.println(String.format("%-12s %-10s %-30s %-20s %10s",
                "Date", "Time", "Description", "Vendor", "Amount"));
        System.out.println(LINE);

        // Transactions
        double total = 0;
        for (Transaction transaction : transactionList) {
            System.out.println(transaction);
            total += transaction.getAmount();
        }

        System.out.println(LINE);
        System.out.println(String.format("Total: %d transactions, Balance: $%.2f",
                transactionList.size(), total));

        // Summary
        long deposits = transactionList.stream().filter(Transaction::isDeposit).count();
        long payments = transactionList.stream().filter(Transaction::isPayment).count();
        double depositTotal = transactionList.stream()
                .filter(Transaction::isDeposit)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double paymentTotal = transactionList.stream()
                .filter(Transaction::isPayment)
                .mapToDouble(Transaction::getAmount)
                .sum();

        System.out.println(String.format("Deposits: %d (%.2f), Payments: %d (%.2f)",
                deposits, depositTotal, payments, paymentTotal));
    }
}