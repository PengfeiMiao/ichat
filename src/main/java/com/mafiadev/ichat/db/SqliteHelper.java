package com.mafiadev.ichat.db;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static com.mafiadev.ichat.constant.Constant.DB_PATH;

public class SqliteHelper {

    private static Connection connection = null;

    private static Connection prepareConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
//            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean validate(Collection<String> tables) {
        boolean isValid = true;
        String sql =
                "SELECT name FROM sqlite_master WHERE type='table' AND name in ('" + String.join("','", tables) + "')";
        try (Connection conn = SqliteHelper.prepareConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            isValid = rs.getRow() == tables.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }

    public static void main(String[] args) {
//        try (Connection conn = SqliteHelper.prepareConnection()) {
//            List<?> select = SqliteHelper.getSchema(conn, "session");
//            System.out.println(select);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        SessionEntity entity = ModelEntityMapper.MAPPER.convertSessionModelToEntity(new GptSession());
//        System.out.println(entity);

        SessionFactory sessionFactory;
        try {
            sessionFactory = HibernateUtil.getSessionFactory(!validate(TableScanner.scanTables()));
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(SessionEntity.builder().userName("test1234").build());
            session.getTransaction().commit();
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
