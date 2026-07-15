package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.StoredFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFileResponse store(MultipartFile file);

    Resource load(String storedName);
}
