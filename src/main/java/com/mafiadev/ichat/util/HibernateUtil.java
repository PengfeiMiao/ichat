package com.mafiadev.ichat.util;

import com.mafiadev.ichat.constant.Constant;
import com.mafiadev.ichat.entity.SessionEntity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Table;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.db.SqliteHelper.validate;

public class HibernateUtil {
    private static final String packagePath = "com.mafiadev.ichat";

    private static volatile SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                    // Add your entity classes
                    configuration.addAnnotatedClass(SessionEntity.class);
                    // Configure other Hibernate properties
                    configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + Constant.DB_PATH);
                    if (!validate(scanTables())) {
                        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
                    } else {
                        configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
                    }

                    sessionFactory = configuration.buildSessionFactory();
                }
            }
        }

        return sessionFactory;
    }

    private static Set<String> scanTables() {
        // 创建 Reflections 实例，指定扫描的包路径和扫描器
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packagePath))
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated));

        // 获取带有@Table注解的类
        return reflections.getTypesAnnotatedWith(Table.class)
                .stream()
                .map(it -> it.getAnnotation(Table.class).name())
                .collect(Collectors.toSet());
    }
}