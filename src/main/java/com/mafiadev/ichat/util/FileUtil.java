package com.mafiadev.ichat.util;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileUtil {
    public static File pngConverter(URI uri) {
        File outputFile = null;
        try (InputStream inputStream = uri.toURL().openStream();
             OutputStream outputStream = Files.newOutputStream(Paths.get(uri.getPath() + ".png"))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputFile = new File(uri.getPath() + ".png");
            Files.delete(Paths.get(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public static void pngCleaner(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".png"))
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}