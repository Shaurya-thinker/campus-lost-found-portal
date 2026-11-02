package com.campus.lostfound.service;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Storage abstraction for private visual evidence.
 * Decouples controllers and domain logic from the underlying storage provider
 * (local filesystem in development, private AWS S3 bucket in production).
 */
public interface EvidenceStorageService {

    /**
     * Stores an evidence file stream and returns an opaque, randomly generated storage key.
     *
     * @param inputStream      data stream of the file
     * @param originalFilename original name provided by client
     * @param contentType      verified MIME type
     * @param fileSize         size in bytes
     * @return opaque unique storage key (e.g. UUID-based)
     */
    String store(InputStream inputStream, String originalFilename, String contentType, long fileSize);

    /**
     * Loads the stored evidence file as a readable Spring Resource.
     *
     * @param storageKey opaque storage key
     * @return Spring Resource
     */
    Resource loadAsResource(String storageKey);

    /**
     * Deletes the stored evidence file.
     *
     * @param storageKey opaque storage key
     */
    void delete(String storageKey);
}

