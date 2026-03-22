package com.codes.pdfConverter.Utile;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;

public class DocxWriter {

    public static File saveAsDocx(String content) throws Exception {
//        To create blank Word doc
        XWPFDocument document = new XWPFDocument();
//        Creates PeraGraph
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(content);

        File file = new File("output.docx");
        FileOutputStream out = new FileOutputStream(file);
        document.write(out);

        out.close();
        document.close();

        return file;
    }
}
