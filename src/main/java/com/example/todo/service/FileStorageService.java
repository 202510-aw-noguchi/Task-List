package com.example.todo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path rootDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        String cleaned = StringUtils.cleanPath(filename).replace("\\", "/");
        if (cleaned.contains("..")) {
            cleaned = cleaned.replace("..", "");
        }
        cleaned = cleaned.replace("/", "");
        cleaned = cleaned.replace("\u0000", "");
        if (cleaned.isBlank()) {
            return "unknown";
        }
        return cleaned;
    }

    public String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Files.createDirectories(rootDir);
        String storedName = UUID.randomUUID().toString();
        Path target = rootDir.resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    public Resource loadAsResource(String storedFilename) throws IOException {
        Path file = rootDir.resolve(storedFilename).normalize();
        if (!file.startsWith(rootDir)) {
            throw new IOException("Invalid file path.");
        }
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found.");
        }
        return resource;
    }

    public void delete(String storedFilename) throws IOException {
        if (storedFilename == null || storedFilename.isBlank()) {
            return;
        }
        Path file = rootDir.resolve(storedFilename).normalize();
        if (!file.startsWith(rootDir)) {
            throw new IOException("Invalid file path.");
        }
        Files.deleteIfExists(file);
    }
}
