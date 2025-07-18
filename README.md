## 📸 Screenshots

| Feature | Screenshot |
|---------|------------|
| Main Interface | ![Main](screenshots/main-menu.png) |
| Add Transaction | ![Transaction](screenshots/add-transaction.png) |
| Reports | ![Reports](screenshots/reports.png) |

# Enhanced Accounting Ledger Application

A Java-based financial transaction management system that allows users to track deposits, payments, and generate comprehensive reports.

## Features

### Core Functionality
- **Add Deposits**: Record incoming transactions with timestamp, description, and vendor
- **Make Payments**: Track outgoing transactions (stored as negative amounts)
- **View Ledger**: Display all transactions, deposits only, or payments only
- **Persistent Storage**: Automatic CSV file storage with pipe-delimited format

### Enhanced Reporting System
- **Month-to-Date**: Current month transactions
- **Previous Month**: Last month's complete transaction history
- **Year-to-Date**: Current year transactions
- **Previous Year**: Last year's complete transaction history
- **Vendor Search**: Find all transactions by vendor name
- **Custom Search**: Advanced filtering by date range, description, vendor, and amount range

### Key Improvements Made

#### 1. **Robust Data Management**
- Custom `Transaction` class with proper encapsulation
- Automatic CSV file creation and management
- Data validation and error handling
- Sorted display (newest transactions first)

#### 2. **Enhanced User Interface**
- Clean, formatted console output with separators
- Input validation for all user entries
- Required field validation
- Error recovery and user-friendly messages

#### 3. **Advanced Reporting**
- Multiple pre-built report types
- Custom search with multiple filter criteria
- Transaction summaries with totals and counts
- Separate deposit/payment analysis

#### 4. **Professional Code Structure**
- Modular design with separate classes for different responsibilities
- Custom exception handling (`LedgerException`)
- Utility classes for input validation and file management
- Stream API usage for efficient data processing

#### 5. **Data Persistence**
- Automatic file backup with header row
- Error handling for file operations
- Data integrity validation
- Graceful handling of corrupted data

## Usage

```bash
javac AccountingLedgerApp.java
java AccountingLedgerApp
```

## File Structure
- `transactions.csv`: Stores all transaction data
- Format: `date|time|description|vendor|amount`

## Navigation
- **Home Screen**: D (Deposit), P (Payment), L (Ledger), X (Exit)
- **Ledger Screen**: A (All), D (Deposits), P (Payments), R (Reports), H (Home)
- **Reports Screen**: 1-6 (Report types), 0 (Back)

## Data Format
- Dates: yyyy-MM-dd
- Time: HH:mm:ss (automatically generated)
- Amounts: Positive for deposits, negative for payments
- All fields are required except in custom search


🚀 Key Improvements:
1. Enhanced Architecture

Separate classes for Transaction, FileManager, InputValidator, and ReportGenerator
Custom exception handling with LedgerException
Stream API for efficient data filtering and processing
Clean separation of concerns

2. Robust Input Validation

Type-safe input methods with proper error handling
Required field validation with user-friendly prompts
Date format validation with clear error messages
Amount validation ensuring positive values for deposits/payments

3. Advanced File Management

Automatic file creation if transactions.csv doesn't exist
Header management for CSV files
Error handling for corrupt data with skip capability
Safe file operations with proper resource management

4. Enhanced User Interface

Professional formatting with consistent separators and headers
Color-coded sections with clear visual hierarchy
Comprehensive transaction display with totals and summaries
Intuitive navigation with clear menu options

5. Advanced Reporting Features

All required reports (Month to Date, Previous Month, Year to Date, Previous Year)
Enhanced vendor search with case-insensitive partial matching
Custom search functionality with multiple filter criteria:

Date range filtering
Description search
Vendor search
Amount range filtering


Detailed summaries showing deposits vs payments counts and totals

6. Smart Data Handling

Automatic sorting (newest transactions first)
Efficient filtering using Java 8 Streams
Memory-efficient operations for large datasets
Graceful error recovery from corrupted data

🛠 Technical Highlights:

Modern Java Features: Uses LocalDate/LocalTime, Streams, Optional patterns
Exception Safety: Comprehensive error handling with user-friendly messages
Performance: Efficient stream operations for filtering and sorting
Maintainability: Clean, well-documented code with logical class separation
User Experience: Intuitive interface with helpful prompts and feedback

📊 Sample Usage:
The application creates a transactions.csv file automatically and provides:

Real-time balance calculations
Transaction summaries with counts and totals
Flexible search capabilities
Professional transaction display

This enhanced version goes well beyond the basic requirements and demonstrates advanced Java programming concepts, making it an excellent portfolio piece for potential employers!
