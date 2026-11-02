package com.campus.lostfound.service;

import com.campus.lostfound.exception.BusinessRuleException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local filesystem implementation of EvidenceStorageService for development and on-premise deployments.
 * Stores evidence files outside the git repository, outside static web assets, and outside public web roots.
 */
@Service
public class LocalEvidenceStorageService implements EvidenceStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalEvidenceStorageService.class);

    private final Path rootLocation;

    public LocalEvidenceStorageService(
            @Value("${app.evidence.storage-path:${EVIDENCE_STORAGE_PATH:E:/CampusLostFoundPrivateUploads}}") String storagePath) {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Initialized secure private evidence storage directory at: {}", rootLocation);
            }
        } catch (IOException e) {
            log.error("Failed to initialize evidence storage directory at: {}", rootLocation, e);
            throw new IllegalStateException("Configured evidence storage path is inaccessible: " + rootLocation, e);
        }
    }

    @Override
    public String store(InputStream inputStream, String originalFilename, String contentType, long fileSize) {
        String extension = resolveExtension(contentType);
        String storageKey = UUID.randomUUID().toString() + extension;

        Path destinationFile = this.rootLocation.resolve(storageKey).normalize().toAbsolutePath();

        // Path traversal defense
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside configured evidence directory.");
        }

        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return storageKey;
        } catch (IOException e) {
            log.error("Failed to write evidence file to disk", e);
            throw new BusinessRuleException("Failed to store private evidence: " + e.getMessage());
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            if (storageKey == null || storageKey.contains("..") || storageKey.contains("/") || storageKey.contains("\\")) {
                throw new SecurityException("Invalid storage key format.");
            }

            Path file = rootLocation.resolve(storageKey).normalize().toAbsolutePath();
            if (!file.startsWith(rootLocation)) {
                throw new SecurityException("Cannot read evidence file outside storage root.");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessRuleException("Evidence file not found or unreadable on disk.");
            }
        } catch (MalformedURLException e) {
            throw new BusinessRuleException("Evidence file resource resolution error: " + e.getMessage());
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.contains("..") || storageKey.contains("/") || storageKey.contains("\\")) {
            return;
        }
        try {
            Path file = rootLocation.resolve(storageKey).normalize().toAbsolutePath();
            if (file.startsWith(rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("Failed to delete evidence file from disk: {}", storageKey, e);
        }
    }

    private String resolveExtension(String contentType) {
        if (contentType == null) return ".bin";
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".bin";
        };
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}

