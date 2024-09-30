package com.mafiadev.ichat.db;

import com.mafiadev.ichat.llm.GptSession;
import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.FileUtil;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void createTable(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            conn.commit();
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
            e.printStackTrace();
        }

        return exists;
    }

    public static void insert(Connection conn, @NonNull String table, Object obj) {
        table = table.toUpperCase();
        StringBuilder sql = new StringBuilder("INSERT INTO " + table + " (");
        Map<String, Object> data = new HashMap<>();

        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                data.put(CommonUtil.convertToSnakeCase(field.getName()), field.get(obj));
                sql.append(CommonUtil.convertToSnakeCase(field.getName())).append(",");
            }

            sql = new StringBuilder(sql.substring(0, sql.length() - 1) + ") VALUES (");

            for (int i = 0; i < data.size(); i++) {
                sql.append("?,");
            }

            sql = new StringBuilder(sql.substring(0, sql.length() - 1) + ")");

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try (PreparedStatement pStmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object value : data.values()) {
                pStmt.setObject(index++, value);
            }

            pStmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> select(Connection conn, @NonNull String table, Class<T> clazz) {
        table = table.toUpperCase();
        List<T> resultList = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + ";");
            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    convert(rs, obj, field);
                }
                resultList.add(obj);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    public void update() {
    }

    public void delete() {
    }

    private static <T> void convert(ResultSet rs, T obj, Field field) {
        try {
            String fieldName = field.getName();
            Object columnValue = rs.getObject(CommonUtil.convertToSnakeCase(fieldName));
            if (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType())) {
                if (columnValue instanceof Integer) {
                    columnValue = Integer.parseInt(String.valueOf(columnValue)) != 0;
                }
                if (columnValue instanceof String) {
                    columnValue = columnValue != "";
                }
            }

            field.setAccessible(true);
            field.set(obj, columnValue);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (Connection conn = SqliteHelper.prepareConnection()) {
            List<GptSession> select = SqliteHelper.select(conn, "session", GptSession.class);
            System.out.println(select);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
