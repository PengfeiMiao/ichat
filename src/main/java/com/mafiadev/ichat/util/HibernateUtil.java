package com.mafiadev.ichat.util;

import com.mafiadev.ichat.constant.Constant;
import com.mafiadev.ichat.entity.SessionEntity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory(boolean initializeDatabase) {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            // Add your entity classes
            configuration.addAnnotatedClass(SessionEntity.class);
            // Configure other Hibernate properties
            configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + Constant.DB_PATH);
            if (initializeDatabase) {
                configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            } else {
                configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
            }

            sessionFactory = configuration.buildSessionFactory();
        }

        return sessionFactory;
    }
}