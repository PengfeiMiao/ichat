package com.mafiadev.ichat.constant;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface Constant {
    Path FILE_PATH = Paths.get(System.getProperty("user.home"), "ichat", "data");


    Path DB_PATH = Paths.get(FILE_PATH.toString(), "test.db");

    String SEARCH_FAILED = "搜索失败";
}
