package com.mafiadev.ichat.db;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Table;
import java.util.Set;
import java.util.stream.Collectors;

public class TableScanner {
    private static final String packagePath = "com.mafiadev.ichat";

    public static Set<String> scanTables() {
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
