package com.codes.pdfConverter.Utile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static File saveAsTxt(String content) throws IOException {
        File file = new File("output.txt");
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
        return file;
    }
}
