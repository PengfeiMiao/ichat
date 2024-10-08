package com.mafiadev.ichat.db;

import com.mafiadev.ichat.annotation.FieldA;
import com.mafiadev.ichat.annotation.TableA;
import com.mafiadev.ichat.annotation.TableScanner;
import com.mafiadev.ichat.util.CommonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.db.SqliteHelper.TYPE_MAP;
import static com.meteor.wechatbc.plugin.PluginLoader.logger;

public class SqlLoader {

    public static Map<String, List<DataField>> TABLE_SCHEMAS = new HashMap<>();

    static {
        applySchemas();
        loadSchemas();
        validateSchemas();
    }

    private static void applySchemas() {
        try (Connection conn = SqliteHelper.prepareConnection()) {
            new SqlLoader().readSQLFiles().forEach((key, value) -> {
                if (!SqliteHelper.exists(conn, key.toUpperCase())) {
                    SqliteHelper.createTable(conn, value);
                }
            });
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> readSQLFiles() throws IOException {
        Map<String, String> sqlQueries = new HashMap<>();

        ClassLoader classLoader = getClass().getClassLoader();
        String folderPath = "sql";
        InputStream folderStream = classLoader.getResourceAsStream(folderPath);

        if (folderStream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(folderStream))) {
                String fileName;
                while ((fileName = br.readLine()) != null) {
                    if (fileName.endsWith(".sql")) {
                        InputStream sqlStream = classLoader.getResourceAsStream(folderPath + "/" + fileName);
                        if (sqlStream != null) {
                            try (BufferedReader sqlReader = new BufferedReader(new InputStreamReader(sqlStream))) {
                                StringBuilder sqlContent = new StringBuilder();
                                String line;
                                while ((line = sqlReader.readLine()) != null) {
                                    sqlContent.append(line).append("\n");
                                }
                                sqlQueries.put(CommonUtil.removeSuffix(fileName, ".sql"), sqlContent.toString());
                            }
                        }
                    }
                }
            }
        }

        return sqlQueries;
    }

    private static void loadSchemas() {
        try (Connection conn = SqliteHelper.prepareConnection()) {
            for (Class<?> tableClz : TableScanner.scanTables()) {
                String tableName = tableClz.getAnnotation(TableA.class).value();
                List<DataField> dataFields = SqliteHelper.getSchema(conn, tableName);
                TABLE_SCHEMAS.put(tableName, dataFields);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void validateSchemas() {
        for (Class<?> tableClz : TableScanner.scanTables()) {
            String tableName = tableClz.getAnnotation(TableA.class).value();
            // 数据库中的表结构
            List<DataField> dataFields = TABLE_SCHEMAS.get(tableName);
            Map<String, String> dateTypeMap = dataFields.stream()
                    .collect(Collectors.toMap(DataField::getName, DataField::getType));
            // Pojo 对象反射信息
            Field[] fields = tableClz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = CommonUtil.convertToSnakeCase(field.getName());
                FieldA fieldA = field.getAnnotation(FieldA.class);
                boolean isDefined = false;
                if(fieldA != null) {
                    fieldName = fieldA.value();
                    isDefined = true;
                }
                List<String> types = TYPE_MAP.get(CommonUtil.getPrimitiveType(field.getType()));
                if(types != null && !types.contains(dateTypeMap.get(fieldName))) {
                    String message = fieldName + " not valid";
                    logger.warn(message);
                    if (isDefined) {
                        throw new RuntimeException(message);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        validateSchemas();
    }
}