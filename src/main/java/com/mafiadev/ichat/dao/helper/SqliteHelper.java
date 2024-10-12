package com.mafiadev.ichat.dao.helper;

import cn.hutool.db.Entity;
import cn.hutool.db.sql.ConditionBuilder;
import cn.hutool.db.sql.SqlBuilder;
import com.mafiadev.ichat.util.CommonUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void batchInsert(Collection<?> list) {
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            list.forEach(session::save);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(Object obj) {
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(Object obj) {
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateBy(Object obj, ConditionBuilder conditionBuilder) {
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            Entity entity = convertEntity(obj);
            session.beginTransaction();
            SqlBuilder sqlBuilder = new SqlBuilder().update(entity).where(conditionBuilder.build());
            NativeQuery<?> query = session.createNativeQuery(sqlBuilder.build());
            AtomicInteger index = new AtomicInteger(1);
            sqlBuilder.getParamValues().forEach(param -> query.setParameter(index.getAndIncrement(), param));
            conditionBuilder.getParamValues().forEach(param -> query.setParameter(index.getAndIncrement(), param));
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> selectBy(Class<T> clazz, ConditionBuilder conditionBuilder) {
        String tableName = CommonUtil.convertToSnakeCase(clazz.getName());
        if (clazz.getAnnotation(Table.class) != null) {
            tableName = clazz.getAnnotation(Table.class).name();
        }
        List<T> resultList = new ArrayList<>();
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
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

    public static long countBy(Class<?> clazz, ConditionBuilder conditionBuilder) {
        String tableName = CommonUtil.convertToSnakeCase(clazz.getName());
        if (clazz.getAnnotation(Table.class) != null) {
            tableName = clazz.getAnnotation(Table.class).name();
        }
        long count = 0;
        SessionFactory sessionFactory = HibernateHelper.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + conditionBuilder.build();
            NativeQuery<?> query = session.createNativeQuery(sql);
            AtomicInteger index = new AtomicInteger(1);
            conditionBuilder.getParamValues().forEach(param -> query.setParameter(index.getAndIncrement(), param));
            Object result = query.getSingleResult();
            count = ((Number) result).longValue();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @NotNull
    private static Entity convertEntity(Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        String tableName = CommonUtil.convertToSnakeCase(clazz.getName());
        if (clazz.getAnnotation(Table.class) != null) {
            tableName = clazz.getAnnotation(Table.class).name();
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, Object> fieldValues = new HashMap<>();
        for (Field field : fields) {
            String fieldName = CommonUtil.convertToSnakeCase(field.getName());
            if (field.getAnnotation(Column.class) != null) {
                fieldName = field.getAnnotation(Column.class).name();
            }
            field.setAccessible(true);
            Object value = field.get(obj);
            if (!Objects.isNull(value)) {
                fieldNames.add(fieldName);
                fieldValues.put(fieldName, value);
            }
            field.setAccessible(false);
        }
        String[] fieldNameArr = fieldNames.toArray(new String[0]);
        Entity entity = Entity.create(tableName);
        entity.addFieldNames(fieldNameArr);
        for(String fieldName : fieldNameArr) {
            entity.set(fieldName, fieldValues.get(fieldName));
        }
        return entity;
    }
}
