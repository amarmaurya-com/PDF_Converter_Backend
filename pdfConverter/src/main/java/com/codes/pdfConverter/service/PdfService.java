package com.codes.pdfConverter.service;


import com.codes.pdfConverter.TaskResponse;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class PdfService implements ServiceInterface {

    @Value("${ilovepdf.public-key}")
    private String publicKey;
    @Value("${ilovepdf.secret-key}")
    private String secretKey;
    private final WebClient apiClient = WebClient.create("https://api.ilovepdf.com/v1");

    @Override
    public byte[] imageToPdf(List<MultipartFile> image) throws IOException {
//        Create new empty PDF Documents
        PDDocument document = new PDDocument();
//        Read image

        for (MultipartFile imageStream : image) {
            BufferedImage bufferedImage = ImageIO.read(imageStream.getInputStream());
//        Create PDF image as the size of image
            PDPage page = new PDPage(
                    new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight())
            );
            document.addPage(page);
            // PDImageXObject xobject = LosslessFactory.createFromImage(document, bufferedImage);
            PDImageXObject xobject = JPEGFactory.createFromImage(document, bufferedImage, 0.75f);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(xobject, 0, 0);
            contentStream.close();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        return baos.toByteArray();
    }


    //    Conver Text to PDF
    @Override
    public byte[] txtToPdf(MultipartFile file) throws IOException {
////        Read Text from file
//        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
//        PDDocument document = new PDDocument();
//        PDPage page = new PDPage();
//        document.addPage(page);
//
//        PDPageContentStream contentStream = new PDPageContentStream(document, page);
////        Defining Property
//        contentStream.beginText();
//        contentStream.setFont(PDType1Font.HELVETICA, 12);
//        contentStream.setLeading(14.4f);   // Space b/w next Line
//        contentStream.newLineAtOffset(50, 750);
//
//        for(String line : text.split("\n")){
//            contentStream.showText(line);
//            contentStream.newLine();
//        }
//
//        contentStream.endText();
//        contentStream.close();
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        document.save(baos);
//        document.close();
//        return baos.toByteArray();

        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        // Remove characters PDFBox can't handle in standard fonts (like \r)
        String[] lines = text.replace("\r", "").split("\n");

        try (PDDocument document = new PDDocument()) {
            float margin = 50;
            float yStart = 750;
            float yPosition = yStart;
            float leading = 14.5f;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.setLeading(leading); // Required for newLine() to work
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yStart);

            for (String line : lines) {
                // Check if we need a new page BEFORE writing
                if (yPosition < margin + leading) {
                    contentStream.endText();
                    contentStream.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.setLeading(leading);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yStart);
                    yPosition = yStart;
                }

                // Sanitize string: showText fails on characters not in font encoding
                contentStream.showText(line.replaceAll("[\\p{Cntrl}&&[^\\t\\n\\r]]", ""));
                contentStream.newLine();
                yPosition -= leading;
            }

            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }

    }

    /*
            PDF-TO-TXT
     */
//      extracts the text of full page at once
    @Override
    public String extractFullText(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();

        String text = stripper.getText(document);
        document.close();   //   cleans the memory after the completion of task
        return text;
    }


    //    Extract Text Page wised
    @Override
    public String extractPageWiseText(MultipartFile file) throws Exception {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();

        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            builder.append("String: ").append(i).append("\n");
            builder.append(stripper.getText(document)).append("\n\n");
        }
        document.close();
        return builder.toString();
    }

//    Compress PDF
    @Override
    public byte[] compressPdf(MultipartFile file) throws Exception {
        PDDocument document = PDDocument.load(file.getInputStream());

        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();

            for (COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);

                if (xObject instanceof PDImageXObject image) {

                    BufferedImage bufferedImage = image.getImage();


                    int newWidth = bufferedImage.getWidth() / 2;
                    int newHeight = bufferedImage.getHeight() / 2;


                    BufferedImage scaledImage = new BufferedImage(
                            newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = scaledImage.createGraphics();

                    g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);
                    g.dispose();

                    // Replace image with compressed JPEG
                    PDImageXObject compressedImage =
                            JPEGFactory.createFromImage(document, scaledImage, 0.6f);

                    resources.put(name, compressedImage);
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

//    Merge Into PDF
    @Override
    public byte[] mergePdfs(List<MultipartFile> files) throws Exception {
        if(files==null || files.isEmpty())
            throw new IllegalArgumentException("At least one file must be provided");

        PDFMergerUtility utility = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (MultipartFile file : files) {
            if(!file.getOriginalFilename().toLowerCase().endsWith(".pdf"))
                throw new IllegalArgumentException("Only pdf files are supported");
            utility.addSource(file.getInputStream());
        }

        utility.setDestinationStream(outputStream);
        utility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return outputStream.toByteArray();
    }

    //    Word-to-pdf
    @Override
    public byte[] wordToPdf(MultipartFile file) throws IOException, InterruptedException {
        File input = File.createTempFile("input-", ".docx");

        file.transferTo(input);

        // 1️⃣ Create task
        TaskResponse taskResponse = apiClient.post()
                .uri("/start")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "public_key", publicKey,
                        "secret_key", secretKey,
                        "tool", "officepdf"
                ))
                .retrieve()
                .bodyToMono(TaskResponse.class)
                .block();

        String server = taskResponse.getServer();
        String task = taskResponse.getTask();

        WebClient serverClient = WebClient.create(server);

// 2️⃣ Upload DOCX (FIXED)
        serverClient.post()
                        .

                uri(uriBuilder -> uriBuilder
                        .path("/upload")
                                .

                        queryParam("task", task)
                                .

                        build())
                        .

                contentType(MediaType.MULTIPART_FORM_DATA)
                        .

                bodyValue(new FileSystemResource(input))
                        .

                retrieve()
                        .

                bodyToMono(Void.class)
                        .

                block();

// 3️⃣ Process
        serverClient.post()
                        .

                uri(uriBuilder -> uriBuilder
                        .path("/process")
                                .

                        queryParam("task", task)
                                .

                        build())
                        .

                contentType(MediaType.APPLICATION_JSON)
                        .

                bodyValue("{}")
                        .

                retrieve()
                        .

                bodyToMono(Void.class)
                        .

                block();

        // 4️⃣ Download PDF
        byte[] pdfBytes = serverClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/download")
                        .queryParam("task", task)
                        .queryParam("output_format", "pdf")
                        .build())
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
        input.delete();
        return pdfBytes;
    }

    //    PPT-to-pdf
    //    Excel-to-pdf
}
