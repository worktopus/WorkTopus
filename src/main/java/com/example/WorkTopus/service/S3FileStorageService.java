package com.example.WorkTopus.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.WorkTopus.projects.dto.response.StoredFileResponse;
import com.example.WorkTopus.projects.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public StoredFileResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        String originalName = file.getOriginalFilename();
        String fileExtension = extractExtension(originalName);
        String storedName = UUID.randomUUID().toString() + (fileExtension.isBlank() ? "" : "." + fileExtension);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 오라클 버킷으로 파일 전송
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, storedName, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);

        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패: " + originalName, e);
        }

        return new StoredFileResponse(
                originalName,
                storedName,
                amazonS3.getUrl(bucket, storedName).toString(),
                fileExtension,
                file.getSize(),
                file.getContentType()
        );
    }

    @Override
    public Resource load(String storedName) {
        var s3Object = amazonS3.getObject(bucket, storedName);
        return new InputStreamResource(s3Object.getObjectContent());
    }

    private String extractExtension(String originalName) {
        if (originalName == null) return "";
        int idx = originalName.lastIndexOf('.');
        return (idx > 0 && idx < originalName.length() - 1) ? originalName.substring(idx + 1) : "";
    }
}