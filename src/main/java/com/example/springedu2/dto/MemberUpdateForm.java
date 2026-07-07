package com.example.springedu2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateForm {

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이내로 입력하세요")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식으로 입력하세요")
    @Size(max = 320, message = "이메일은 320자 이내로 입력하세요")
    private String email;

    @Size(max = 100, message = "비밀번호는 100자 이내로 입력하세요")
    private String password;

    private String role = "USER";
    private boolean enabled = true;

}
