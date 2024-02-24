package com.marcosisocram.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DbConnection {
    private static volatile DbConnection instance = null; // Thread-safe singleton instance
    private final Connection conn;
    private static final Logger logger = LoggerFactory.getLogger(DbConnection.class);

    private DbConnection() throws SQLException {
        final String jdbcUrl = Optional.ofNullable(System.getenv("DB_URL")).orElse("jdbc:postgresql://localhost:5432/postgres");
        final String username = Optional.ofNullable(System.getenv("DB_USER")).orElse("user");
        final String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("rinha-de-bK");

        conn = DriverManager.getConnection(jdbcUrl, username, password);
        logger.atDebug().setMessage("Conexao nova").log();
    }

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.conn.isClosed()) {
            synchronized (DbConnection.class) { // Thread-safe synchronization
                if (instance == null || instance.conn.isClosed()) {
                    instance = new DbConnection();
                }
            }
        }
        return instance.conn;
    }
}
