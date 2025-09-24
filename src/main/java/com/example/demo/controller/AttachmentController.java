package com.example.demo.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AttachmentController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final Map<String, Path> ID_TO_FILE = new HashMap<>();

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "conversationId", required = false) String conversationId) {
        try {
            if (!Files.exists(UPLOAD_DIR)) {
                Files.createDirectories(UPLOAD_DIR);
            }
            String id = UUID.randomUUID().toString();
            String original = file.getOriginalFilename();
            String safeName = (original == null || original.isBlank()) ? (id + ".bin") : original;
            Path dest = UPLOAD_DIR.resolve(id + "_" + safeName);
            Files.copy(file.getInputStream(), dest);
            ID_TO_FILE.put(id, dest);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", id);
            resp.put("name", safeName);
            resp.put("size", file.getSize());
            resp.put("url", "/api/attachments/" + id + "/download");
            resp.put("conversationId", conversationId);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable String id) throws IOException {
        Path file = ID_TO_FILE.get(id);
        if (file == null || !Files.exists(file)) return ResponseEntity.notFound().build();
        byte[] bytes = Files.readAllBytes(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName().toString());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}


