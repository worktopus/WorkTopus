package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.StoredFileResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
/**
 * 로컬 파일 시스템에 파일을 저장하고 조회하는 서비스 구현체.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR_ENV = "WORKTOPUS_UPLOAD_DIR";

    private final Path uploadRoot;

    public LocalFileStorageService() {
        this.uploadRoot = resolveUploadRoot();
    }

    @Override
    public StoredFileResponse store(MultipartFile file) {
        // 업로드 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        // 저장할 파일명 생성
        String originalName = cleanOriginalName(file.getOriginalFilename());
        String fileExtension = extractExtension(originalName);
        String storedName = createStoredName(fileExtension);
        Path targetPath = uploadRoot.resolve(storedName).normalize();

        // 업로드 디렉터리 외부 접근 차단
        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("올바르지 않은 파일 경로입니다.");
        }

        try {
            // 파일 저장
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("파일을 저장할 수 없습니다: " + originalName, e);
        }

        return new StoredFileResponse(
                originalName,
                storedName,
                null,
                fileExtension,
                file.getSize(),
                file.getContentType()
        );
    }

    @Override
    public Resource load(String storedName) {
        // 저장 파일명 유효성 검증
        if (storedName == null || storedName.isBlank()) {
            throw new IllegalArgumentException("저장 파일명이 없습니다.");
        }

        Path storedPath = Path.of(storedName);

        // 업로드 디렉터리 외부 접근 차단 및 파일 존재 여부 확인
        if (storedPath.isAbsolute() || storedPath.getNameCount() != 1) {
            throw new IllegalArgumentException("올바르지 않은 저장 파일명입니다.");
        }

        Path targetPath = uploadRoot.resolve(storedPath).normalize();

        if (!targetPath.startsWith(uploadRoot)
                || !Files.exists(targetPath)
                || !Files.isRegularFile(targetPath)
                || !Files.isReadable(targetPath)) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        }

        return new FileSystemResource(targetPath);
    }

    private Path resolveUploadRoot() {
        // 환경 변수 또는 기본 업로드 경로 결정
        String configuredPath = System.getenv(UPLOAD_DIR_ENV);
        Path path;

        if (configuredPath == null || configuredPath.isBlank()) {
            path = Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "worktopus",
                    "uploads"
            );
        } else {
            path = Path.of(configuredPath);
        }

        Path normalizedPath = path.toAbsolutePath().normalize();
        Path projectPath = Path.of(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();

        // 프로젝트 소스 내부 경로 사용 방지
        if (normalizedPath.startsWith(projectPath)) {
            throw new IllegalStateException("업로드 경로는 프로젝트 소스 외부에 있어야 합니다.");
        }

        return normalizedPath;
    }

    // 원본 파일명 정리 및 검증
    private String cleanOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("원본 파일명이 없습니다.");
        }

        String normalizedName = originalName.replace('\\', '/');
        String cleanName = normalizedName.substring(normalizedName.lastIndexOf('/') + 1).trim();

        if (cleanName.isBlank()
                || ".".equals(cleanName)
                || "..".equals(cleanName)
                || cleanName.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
        }

        return cleanName;
    }

    private String extractExtension(String originalName) {
        int extensionIndex = originalName.lastIndexOf('.');

        if (extensionIndex <= 0 || extensionIndex == originalName.length() - 1) {
            return "";
        }

        return originalName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String createStoredName(String fileExtension) {
        // UUID 기반 저장 파일명 생성
        String uuid = UUID.randomUUID().toString();

        if (fileExtension.isBlank()) {
            return uuid;
        }

        return uuid + "." + fileExtension;
    }
}
