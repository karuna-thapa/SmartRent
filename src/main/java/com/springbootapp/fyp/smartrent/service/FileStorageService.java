package com.springbootapp.fyp.smartrent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeVehicleImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(uploadDir, "vehicles");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image"
        );
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID() + ext;

        Files.copy(file.getInputStream(),
                uploadPath.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/vehicles/" + filename;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            // fileUrl is like /uploads/vehicles/abc.jpg
            Path filePath = Paths.get(uploadDir + fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
