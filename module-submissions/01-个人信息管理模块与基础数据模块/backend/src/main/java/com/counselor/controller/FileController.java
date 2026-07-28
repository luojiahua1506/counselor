package com.counselor.controller;

import com.counselor.service.LocalPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class FileController {

    private final LocalPhotoService localPhotoService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        String key = localPhotoService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", "/api/public/image?key=" + key));
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam("key") String key) {
        byte[] data = localPhotoService.getObject(key);
        String ct = "image/jpeg";
        if (key.endsWith(".png")) ct = "image/png";
        else if (key.endsWith(".gif")) ct = "image/gif";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).body(data);
    }
}
