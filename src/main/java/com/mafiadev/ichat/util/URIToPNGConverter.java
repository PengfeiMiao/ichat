package com.mafiadev.ichat.util;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class URIToPNGConverter {
    public static File convert(URI uri) {
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
}