# Internet Banking & Management System (NSP Lab Project)

A Java Swing desktop application with MySQL database connectivity through JDBC. It is an **educational demonstration**, not production banking software. Use fictional data only.

## Features
- Customer registration and login
- Automatic demo account number generation
- View account balance
- Deposit and withdrawal with insufficient-balance validation
- Transfer between registered demo accounts
- Transaction history
- JDBC `PreparedStatement` queries and SQL transactions for money movement

## Requirements
- JDK 17+
- Apache Maven
- MySQL Server 8+

## Setup
1. Start MySQL Server.
2. Open MySQL Workbench or the MySQL command line and execute `database/schema.sql`.
3. Configure the connection. Defaults are `localhost:3306`, database `internet_banking`, user `root`, and a blank password. Prefer environment variables:
   - `BANK_DB_URL` (optional; default shown in `DB.java`)
   - `BANK_DB_USER`
   - `BANK_DB_PASSWORD`
4. From the project root, run:
   ```bash
   mvn clean package
   mvn exec:java -Dexec.mainClass=edu.nsp.banking.BankingApp
   ```
   If the `exec:java` goal is unavailable in your Maven setup, run the main class from your IDE after importing the project as a Maven project.

## Suggested demo
1. Register two fictional customers using different email addresses.
2. Log in to the first customer.
3. Deposit a small demo amount.
4. Log out, register/login as the second customer, and note their account number.
5. Log back in as the first customer and transfer to the second account number.
6. Open transaction history and verify the balance.

## GitHub submission
1. Create a new **public** GitHub repository (unless your college specifies private).
2. Upload the extracted project folder contents, keeping the directory structure.
3. Do not upload real passwords, bank details, `.env` files, or personal data.
4. Copy the repository URL into your college Google Form.

## Important academic/security notes
- This project is a lab prototype. It is not connected to real banks or payment networks.
- SHA-256 is used as a simple password-hashing demonstration. Real systems should use a salted password hashing algorithm such as Argon2id, bcrypt, or PBKDF2, plus HTTPS, MFA, audit controls, rate limiting, and extensive security review.
- Transfers are performed in a database transaction and account rows are locked while balances are updated.
- Account numbers are randomly generated for demonstration; production systems require collision handling, stronger identity verification, and regulated controls.
