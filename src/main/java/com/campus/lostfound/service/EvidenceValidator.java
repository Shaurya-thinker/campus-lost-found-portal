package com.campus.lostfound.service;

import com.campus.lostfound.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Deep validator for uploaded private visual evidence.
 * Enforces format whitelist (JPEG, PNG, WebP), file size constraints, and verifies
 * binary magic bytes and decodability rather than trusting client headers or extensions alone.
 */
@Component
public class EvidenceValidator {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final long maxFileSizeBytes;

    public EvidenceValidator(@Value("${app.evidence.max-file-size-bytes:5242880}") long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    /**
     * Validates an uploaded evidence image file.
     *
     * @param file uploaded multipart file
     * @throws BusinessRuleException on invalid, oversized, or spoofed file
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Evidence photo cannot be empty.");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessRuleException(String.format(
                    "Evidence photo exceeds the maximum allowed size of 5 MB (size: %.2f MB).",
                    file.getSize() / (1024.0 * 1024.0)
            ));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException("Unsupported file type. Only JPEG, PNG, and WebP images are permitted.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessRuleException("Unable to read uploaded evidence file content.");
        }

        if (bytes.length < 12) {
            throw new BusinessRuleException("Uploaded file is too small or corrupted to be a valid image.");
        }

        // Prevent script and executable uploads disguised as images
        if (isExecutableOrScript(bytes)) {
            throw new BusinessRuleException("Executable or script files are strictly prohibited.");
        }

        // Deep binary header verification
        if (isJpeg(bytes)) {
            verifyDecodable(bytes, "JPEG");
        } else if (isPng(bytes)) {
            verifyDecodable(bytes, "PNG");
        } else if (isWebp(bytes)) {
            verifyWebp(bytes);
        } else {
            throw new BusinessRuleException("File contents do not match any supported image format (JPEG, PNG, WebP).");
        }
    }

    private boolean isJpeg(byte[] bytes) {
        return (bytes[0] & 0xFF) == 0xFF &&
               (bytes[1] & 0xFF) == 0xD8 &&
               (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return (bytes[0] & 0xFF) == 0x89 &&
               (bytes[1] & 0xFF) == 0x50 &&
               (bytes[2] & 0xFF) == 0x4E &&
               (bytes[3] & 0xFF) == 0x47 &&
               (bytes[4] & 0xFF) == 0x0D &&
               (bytes[5] & 0xFF) == 0x0A &&
               (bytes[6] & 0xFF) == 0x1A &&
               (bytes[7] & 0xFF) == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        // RIFF header (bytes 0-3: 'R', 'I', 'F', 'F') and WEBP signature (bytes 8-11: 'W', 'E', 'B', 'P')
        return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
               bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private boolean isExecutableOrScript(byte[] bytes) {
        // Windows PE / MZ executable
        if (bytes[0] == 'M' && bytes[1] == 'Z') {
            return true;
        }
        // Linux ELF
        if (bytes[0] == 0x7F && bytes[1] == 'E' && bytes[2] == 'L' && bytes[3] == 'F') {
            return true;
        }
        // Shell script shebang '#!'
        if (bytes[0] == '#' && bytes[1] == '!') {
            return true;
        }
        // Inspect beginning for html or php tags
        String headerPrefix = new String(bytes, 0, Math.min(bytes.length, 128)).toLowerCase();
        return headerPrefix.contains("<?php") ||
               headerPrefix.contains("<script") ||
               headerPrefix.contains("<html>");
    }

    private void verifyDecodable(byte[] bytes, String format) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new BusinessRuleException("The uploaded " + format + " file could not be decoded as a valid image.");
            }
        } catch (BusinessRuleException bre) {
            throw bre;
        } catch (Exception e) {
            throw new BusinessRuleException("Malformed or unreadable " + format + " image file: " + e.getMessage());
        }
    }

    private void verifyWebp(byte[] bytes) {
        if (bytes.length < 16) {
            throw new BusinessRuleException("WebP image file header is truncated.");
        }
        // Verify valid WebP chunk header (VP8, VP8L, or VP8X)
        boolean hasVp8Chunk = (bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8');
        if (!hasVp8Chunk) {
            throw new BusinessRuleException("WebP image does not contain a recognized VP8 bitstream chunk.");
        }
    }
}

