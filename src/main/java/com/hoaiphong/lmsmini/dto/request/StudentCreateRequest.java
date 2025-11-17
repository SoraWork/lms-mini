package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {

    @NotBlank(message = "Tên sinh viên không được để trống")
    @Size(min = 3, max = 100, message = "Tên sinh viên phải từ 3–100 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

}
