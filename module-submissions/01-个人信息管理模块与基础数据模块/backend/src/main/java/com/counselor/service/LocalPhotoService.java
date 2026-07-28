package com.counselor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalPhotoService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    @Value("${app.photo.local-dir:uploads/photos}")
    private String localDir;

    public String uploadFile(MultipartFile file) {
        validate(file);
        String extension = extensionOf(file.getOriginalFilename());
        String objectName = "counselor/" + UUID.randomUUID() + extension;
        Path target = resolveLocalPath(objectName);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return objectName;
        } catch (IOException exception) {
            throw new RuntimeException("照片保存失败", exception);
        }
    }

    public byte[] getObject(String objectName) {
        Path file = resolveLocalPath(objectName);
        if (!Files.isRegularFile(file)) {
            throw new RuntimeException("照片不存在");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException exception) {
            throw new RuntimeException("照片读取失败", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择照片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("照片不能超过5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("仅支持JPG、PNG格式");
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return ".jpg";
        }
        String lowerName = originalFilename.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".png") ? ".png" : ".jpg";
    }

    private Path resolveLocalPath(String objectName) {
        Path root = Paths.get(localDir).toAbsolutePath().normalize();
        Path file = root.resolve(objectName).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("无效的照片路径");
        }
        return file;
    }
}
