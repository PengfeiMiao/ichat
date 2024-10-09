package com.mafiadev.ichat.dao.helper;

import cn.hutool.db.sql.ConditionBuilder;
import com.mafiadev.ichat.util.CommonUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.persistence.Table;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        try (SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void batchInsert(Collection<?> list) {
        try (SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            list.forEach(session::save);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(Object obj) {
        try (SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(Object obj) {
        try (SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> select(Class<T> clazz, ConditionBuilder conditionBuilder) {
        String tableName = CommonUtil.convertToSnakeCase(clazz.getName());
        if (clazz.getAnnotation(Table.class) != null) {
            tableName = clazz.getAnnotation(Table.class).name();
        }
        List<T> resultList = new ArrayList<>();
        try (SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String sql = "SELECT * FROM " + tableName + " WHERE " + conditionBuilder.build();
            Query<T> query = session.createNativeQuery(sql, clazz);
            AtomicInteger index = new AtomicInteger(1);
            conditionBuilder.getParamValues().forEach(param -> query.setParameter(index.getAndIncrement(), param));
            resultList = query.list();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
