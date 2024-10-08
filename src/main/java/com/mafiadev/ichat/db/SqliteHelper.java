package com.mafiadev.ichat.db;

import com.mafiadev.ichat.util.CommonUtil;
import com.mafiadev.ichat.util.FileUtil;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mafiadev.ichat.constant.Constant.FILE_PATH;
import static com.meteor.wechatbc.plugin.PluginLoader.logger;

public class SqliteHelper {
    public static final Path DB_PATH = Paths.get(FILE_PATH.toString(), "test.db");
    public static final Map<Class<?>, List<String>> TYPE_MAP = new HashMap<Class<?>, List<String>>() {{
        put(String.class, Arrays.asList("VARCHAR", "TEXT"));
        put(int.class, Arrays.asList("INT", "INTEGER"));
        put(long.class, Arrays.asList("BIGINT"));
        put(boolean.class, Arrays.asList("TINYINT"));
    }};

    // todo: 多线程问题
    private static Connection connection = null;

    static {
        logger.info("Database location: " + DB_PATH);
        FileUtil.mkFile(DB_PATH);
    }

    public static Connection prepareConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
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
        logger.info("Table created successfully");
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

    public static List<DataField> getSchema(Connection conn, @NonNull String table) {
        table = table.toUpperCase();
        List<DataField> dataFields = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " limit 1;");
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i < metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i);
                dataFields.add(DataField.builder().name(columnName).type(columnType).build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataFields;
    }


    public static <T> List<T> selectOne(Connection conn, @NonNull String table, Class<T> clazz,
                                        Map<String, Object> filter) {
        return select(conn, table, clazz, filter, 1);
    }

    public static <T> List<T> select(Connection conn, @NonNull String table, Class<T> clazz, Map<String, Object> filter,
                                     int limit) {
        table = table.toUpperCase();
        List<T> resultList = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " limit " + limit + ";");
            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
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

    public static void insert(Connection conn, @NonNull String table, Object obj) {
        table = table.toUpperCase();
        StringBuilder sql = new StringBuilder("INSERT INTO " + table + " (");
        Map<String, Object> data = new HashMap<>();

        try {
            for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
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

    public void update() {
    }

    public void delete() {
    }

    private static <T> void convert(ResultSet rs, T obj, java.lang.reflect.Field field) {
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
            List<?> select = SqliteHelper.getSchema(conn, "session");
            System.out.println(select);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
