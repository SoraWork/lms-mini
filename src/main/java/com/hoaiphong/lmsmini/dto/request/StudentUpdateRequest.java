package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateRequest {

    @NotBlank(message = "error.student.name.blank")
    private String name;

    @NotBlank(message = "error.student.email.blank")
    @Email(message = "error.student.email.invalid")
    private String email;

    private List<Long> deleteImageIds;

    private List<String> images;
}
