package com.example.WorkTopus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreateForm {

    @NotBlank(message = "프로젝트 이름을 입력해주세요.")
    @Size(max = 100, message = "프로젝트 이름은 100자 이하입니다.")
    private String name;

    @Size(max = 500, message = "프로젝트 설명은 500자 이하입니다.")
    private String description;

}