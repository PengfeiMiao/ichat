package com.mafiadev.ichat.db;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public static void insert(Object obj) {
        try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(Object obj) {
        try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(Object obj) {
        try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> select(Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String hql = "FROM " + clazz.getName();
            Query<T> query = session.createQuery(hql, clazz);
//            query.setParameter("userName", userName);
            resultList = query.list();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
