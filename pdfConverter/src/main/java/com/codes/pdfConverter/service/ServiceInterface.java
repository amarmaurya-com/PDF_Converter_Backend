package com.codes.pdfConverter.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServiceInterface {
    byte[] imageToPdf(List<MultipartFile> image) throws IOException;
    byte[] txtToPdf(MultipartFile file) throws IOException;
    String extractFullText(MultipartFile file) throws IOException;
    String extractPageWiseText(MultipartFile file) throws Exception;
    byte[] wordToPdf(MultipartFile file) throws IOException, InterruptedException;
    byte[] compressPdf(MultipartFile file) throws Exception;
    byte[] mergePdfs(List<MultipartFile> files) throws Exception;
}
