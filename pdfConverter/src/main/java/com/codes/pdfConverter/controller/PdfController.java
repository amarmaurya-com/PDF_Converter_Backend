package com.codes.pdfConverter.controller;

import com.codes.pdfConverter.Utile.DocxWriter;
import com.codes.pdfConverter.Utile.FileUtil;
import com.codes.pdfConverter.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PdfController {
    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
//      Convert To file
    @PostMapping("/img-to-pdf")
    public ResponseEntity<byte[]> convertToPdf(@RequestParam("files") List<MultipartFile> files) throws Exception {
        byte[] pdf = pdfService.imageToPdf(files);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

//    PDF to Text
    @PostMapping("/pdf-to-txt")
    public ResponseEntity<byte[]> pdfToTxt(@RequestParam("file") MultipartFile file) throws Exception {

        String extractedText = pdfService.extractFullText(file);

        byte[] textBytes = extractedText.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(textBytes);
    }

//    Text to PDF
    @PostMapping("/txt-to-pdf")
    public ResponseEntity<byte[]> txtToPdf(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfService.txtToPdf(file));
    }


//    Word-to-pdf
    @PostMapping(value = "/docx-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertDocxToPdf(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        byte[] pdfBytes = pdfService.wordToPdf(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
//    Excel-to-pdf
//    PPT-to-pdf
//    Compress PDF
    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressPdf(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        byte[] compressedPdf = pdfService.compressPdf(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=compressed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(compressedPdf);
    }

//    Merge into PDF
    @PostMapping(
            value = "/merge",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {

        byte[] mergedPdf = pdfService.mergePdfs(files);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=merged.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(mergedPdf);
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPdf(@RequestParam MultipartFile file,
                                        @RequestParam(defaultValue = "full") String extractType,
                                        @RequestParam(defaultValue = "text") String outputType) throws Exception {
        String extractText;
        if("page".equalsIgnoreCase(extractType))
            extractText=pdfService.extractPageWiseText(file);
        else
            extractText=pdfService.extractFullText(file);

        if("text".equalsIgnoreCase(outputType))
            return ResponseEntity.ok(extractText);

        File outputFile;
        if ("txt".equalsIgnoreCase(outputType))
            outputFile = FileUtil.saveAsTxt(extractText);
        else if("pdf".equalsIgnoreCase(outputType))
            outputFile= DocxWriter.saveAsDocx(extractText);
        else
            return ResponseEntity.badRequest().body("Invalid output type");
        return ResponseEntity.ok("File saved successfully" + outputFile.getName()   );
    }

}
