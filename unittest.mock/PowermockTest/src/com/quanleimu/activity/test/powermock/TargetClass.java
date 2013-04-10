package com.quanleimu.activity.test.powermock;

import java.io.File;

public class TargetClass {
    public boolean createDirectoryStructure(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists()) {
                throw new IllegalArgumentException("\"" + directoryPath + "\" already exists.");
        }

        return directory.mkdirs();
}
}
