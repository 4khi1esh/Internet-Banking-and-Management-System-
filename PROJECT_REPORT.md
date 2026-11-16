# Internet Banking & Management System

## Abstract
The Internet Banking & Management System is a Java desktop application developed using Swing for the graphical user interface and MySQL for persistent storage. JDBC is used to connect the application to the database. The prototype demonstrates common banking operations using fictional accounts.

## Objectives
- Build a user-friendly GUI for basic banking operations.
- Store customer, account, and transaction data in MySQL.
- Demonstrate JDBC connectivity, prepared statements, and transaction management.
- Validate deposits, withdrawals, and transfers.

## Technology stack
Java 17, Swing, JDBC, MySQL 8, Maven.

## Modules
1. Registration and authentication
2. Account and balance management
3. Deposit and withdrawal
4. Inter-account transfer
5. Transaction history

## Database design
- `customers`: customer identity and password hash.
- `accounts`: account number, owner and current balance.
- `transactions`: transaction type, amount, source/destination account and timestamp.

## How to run
See the setup and run instructions in the root `README.md`. Execute `database/schema.sql` before launching the application.

## Conclusion
The project demonstrates how a Java GUI can interact with a relational database using JDBC. It provides a foundation for learning database connectivity and transaction handling. It is a classroom prototype only and must not be used to store or move real money.
