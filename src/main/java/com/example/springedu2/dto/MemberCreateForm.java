package com.example.springedu2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreateForm {

    @NotBlank(message = "아이디는 필수입니다")  // NULL, "(빈문자열)", "(공백)"
    @Size(min = 4, max = 30, message = "아이디는 4~30자로 입력하세요")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max = 100, message = "비밀번호는 4자이상 입력하세요")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이내로 입력하세요")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식으로 입력하세요")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
            message = "이메일 도메인에는 . 이 포함되어야 합니다"
    )
    @Size(max = 320, message = "이메일은 320자 이내로 입력하세요")
    private String email;

    private String role = "USER";

}
