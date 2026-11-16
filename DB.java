package edu.nsp.banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    private DB() {}
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("BANK_DB_URL",
                "jdbc:mysql://localhost:3306/internet_banking?useSSL=false&serverTimezone=UTC");
        String user = System.getenv().getOrDefault("BANK_DB_USER", "root");
        String password = System.getenv().getOrDefault("BANK_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }
}
