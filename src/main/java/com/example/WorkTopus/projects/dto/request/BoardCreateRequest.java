package com.example.WorkTopus.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
<<<<<<< HEAD
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BoardCreateRequest(

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        String content,

        @NotBlank
        String category,

        @Size(max = 200)
        String tag,

        List<MultipartFile> files
) {

    public BoardCreateRequest withTag(String tag) {
        return new BoardCreateRequest(
                title,
                content,
                category,
                tag != null ? tag : this.tag,
                files
        );
    }
}
=======
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardCreateRequest(

        @NotNull
        Long projectId,

        @NotBlank
        @Size(max = 200)
        String title,

        String content,

        //@NotBlank
       // @Size(max = 100)
        //String writerName,

        @NotBlank
        String category,

        boolean notice
) {
}
>>>>>>> origin/feature/admin
