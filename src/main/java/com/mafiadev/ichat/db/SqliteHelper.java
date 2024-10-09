package com.mafiadev.ichat.db;

import com.mafiadev.ichat.entity.SessionEntity;
import com.mafiadev.ichat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class SqliteHelper {

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
            sessionFactory = HibernateUtil.getSessionFactory(true);
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
