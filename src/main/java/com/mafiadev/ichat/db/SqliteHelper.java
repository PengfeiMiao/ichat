package com.mafiadev.ichat.db;

import com.mafiadev.ichat.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;

public class SqliteHelper {
    public static final Path DB_PATH = Paths.get(FILE_PATH.toString(), "test.db");
    private static Connection connection = null;

    static {
        System.out.println(DB_PATH);
        FileUtil.mkFile(DB_PATH);
    }

    public static Connection prepareConnection() {
        if (connection != null) {
            return connection;
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void createTable(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Table created successfully");
    }

    public static boolean exists(Connection conn, String tableName) {
        boolean exists = false;
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return exists;
    }

    public static void main(String[] args) {
        new SqlLoader();
    }
}
