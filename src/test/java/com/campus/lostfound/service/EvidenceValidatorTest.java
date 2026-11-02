package com.campus.lostfound.service;

import com.campus.lostfound.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceValidatorTest {

    private EvidenceValidator validator;

    @BeforeEach
    void setUp() {
        // 5 MB limit
        validator = new EvidenceValidator(5 * 1024 * 1024);
    }

    private byte[] createValidPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private byte[] createValidJpegBytes() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", baos);
        return baos.toByteArray();
    }

    private byte[] createValidWebpBytes() {
        // Minimal valid WebP structure: RIFF(4) + size(4) + WEBP(4) + VP8 (4)
        byte[] bytes = new byte[32];
        bytes[0] = 'R'; bytes[1] = 'I'; bytes[2] = 'F'; bytes[3] = 'F';
        bytes[4] = 24; bytes[5] = 0; bytes[6] = 0; bytes[7] = 0;
        bytes[8] = 'W'; bytes[9] = 'E'; bytes[10] = 'B'; bytes[11] = 'P';
        bytes[12] = 'V'; bytes[13] = 'P'; bytes[14] = '8'; bytes[15] = ' ';
        return bytes;
    }

    @Test
    @DisplayName("Should successfully validate a valid PNG image")
    void testValidateImage_ValidPng() throws Exception {
        byte[] pngBytes = createValidPngBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngBytes);

        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    @DisplayName("Should successfully validate a valid JPEG image")
    void testValidateImage_ValidJpeg() throws Exception {
        byte[] jpegBytes = createValidJpegBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes);

        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    @DisplayName("Should successfully validate a valid WebP image")
    void testValidateImage_ValidWebp() {
        byte[] webpBytes = createValidWebpBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.webp", "image/webp", webpBytes);

        assertDoesNotThrow(() -> validator.validateImage(file));
    }

    @Test
    @DisplayName("Should reject empty file")
    void testValidateImage_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("Should reject oversized file (> 5 MB)")
    void testValidateImage_OversizedFile() {
        // 5.5 MB dummy array
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 500 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", largeBytes);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("exceeds the maximum allowed size"));
    }

    @Test
    @DisplayName("Should reject unsupported content type (e.g. text/plain)")
    void testValidateImage_UnsupportedContentType() {
        byte[] textBytes = "This is a plain text file pretending to be evidence".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", textBytes);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    @DisplayName("Should reject spoofed file: text file disguised with image/jpeg Content-Type")
    void testValidateImage_SpoofedJpeg() {
        byte[] textBytes = "Fake image content here".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", textBytes);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("File contents do not match any supported image format"));
    }

    @Test
    @DisplayName("Should reject executable files disguised with image/png Content-Type")
    void testValidateImage_ExecutableDisguisedAsImage() {
        byte[] exeBytes = new byte[100];
        exeBytes[0] = 'M'; exeBytes[1] = 'Z'; // Windows PE header
        MockMultipartFile file = new MockMultipartFile("file", "malware.png", "image/png", exeBytes);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("Executable or script files are strictly prohibited"));
    }

    @Test
    @DisplayName("Should reject script files containing HTML/PHP headers")
    void testValidateImage_ScriptDisguisedAsImage() {
        byte[] scriptBytes = "<?php echo 'attack'; ?>".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "shell.jpg", "image/jpeg", scriptBytes);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> validator.validateImage(file));
        assertTrue(ex.getMessage().contains("Executable or script files are strictly prohibited"));
    }
}

