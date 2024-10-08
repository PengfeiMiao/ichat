package com.mafiadev.ichat.annotation;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class TableScanner {
    private static final String packagePath = "com.mafiadev.ichat";

    public static Set<Class<?>> scanTables() {
        // 创建 Reflections 实例，指定扫描的包路径和扫描器
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packagePath))
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated));

        // 获取带有@Table注解的类
        return reflections.getTypesAnnotatedWith(TableA.class);
    }
}
