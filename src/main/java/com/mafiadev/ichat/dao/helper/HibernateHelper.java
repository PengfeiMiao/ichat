package com.mafiadev.ichat.dao.helper;

import com.mafiadev.ichat.constant.Constant;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Table;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mafiadev.ichat.dao.helper.SqliteHelper.validate;

public class HibernateHelper {
    private static final String packagePath = "com.mafiadev.ichat";

    private static volatile SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            synchronized (HibernateHelper.class) {
                if (sessionFactory == null || sessionFactory.isClosed()) {
                    Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                    Map<String, Class<?>> scannedTables = scanTables();
                    // Add entity classes
                    scannedTables.values().forEach(configuration::addAnnotatedClass);
                    // Configure other Hibernate properties
                    configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + Constant.DB_PATH);
                    String hbm2ddlMethod = "hibernate.hbm2ddl.auto";
                    if (!validate(scannedTables.keySet()) && configuration.getProperty(hbm2ddlMethod).equals("validate")) {
                        configuration.setProperty(hbm2ddlMethod, "update");
                    } else {
                        configuration.setProperty(hbm2ddlMethod, "validate");
                    }

                    sessionFactory = configuration.buildSessionFactory();
                }
            }
        }

        return sessionFactory;
    }

    private static Map<String, Class<?>> scanTables() {
        // 创建 Reflections 实例，指定扫描的包路径和扫描器
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packagePath))
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated));

        // 获取带有@Table注解的类
        return reflections.getTypesAnnotatedWith(Table.class)
                .stream()
                .collect(Collectors.toMap(
                        it -> it.getAnnotation(Table.class).name(),
                        it -> it,
                        (_old, _new) -> _old
                ));
    }
}