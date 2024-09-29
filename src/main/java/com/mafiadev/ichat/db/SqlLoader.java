package com.mafiadev.ichat.db;

import com.mafiadev.ichat.util.CommonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqlLoader {

    static {
        try(Connection conn = SqliteHelper.prepareConnection()) {
            new SqlLoader().readSQLFiles().forEach((key, value) -> {
                System.out.println(key + ": " + value);
                if(!SqliteHelper.exists(conn, key.toUpperCase())) {
                    SqliteHelper.createTable(conn, value);
                }
            });
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
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
}