package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {

    @NotBlank(message = "error.student.name.blank")
    @Size(min = 3, max = 100, message = "error.student.name.size")
    private String name;

    @NotBlank(message = "error.student.email.blank")
    @Email(message = "error.student.email.invalid")
    private String email;

}
