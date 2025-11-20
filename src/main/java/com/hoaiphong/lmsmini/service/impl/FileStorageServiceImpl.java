package com.hoaiphong.lmsmini.service.impl;


import com.hoaiphong.lmsmini.exception.SomeThingWrongException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl {

    private final Path uploadDir = Paths.get("static/uploads");

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new SomeThingWrongException("error.file.dir.create.failed");
        }
    }

    public String save(MultipartFile file) {
        // Kiểm tra file null hoặc empty
        if (file == null || file.isEmpty()) {
            throw new SomeThingWrongException("error.file.empty");
        }

        if (file.getSize() > 30 * 1024 * 1024) {  // 30MB
            throw new SomeThingWrongException("error.file.too.large");
        }

        try {
            // Tạo tên file unique
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            // Copy file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/static/uploads/" + fileName;
        } catch (IOException e) {
            throw new SomeThingWrongException("error.file.upload.failed");
        }
    }
}

