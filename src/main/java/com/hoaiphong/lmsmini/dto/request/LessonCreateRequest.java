package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequest {

    @NotNull(message = "CourseId không được để trống")
    @Positive(message = "CourseId phải > 0")
    private Long courseId;

    @NotNull(message = "Danh sách bài học không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 bài học")
    @Valid // validate các LessonItem
    private List<LessonItem> lessons;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonItem {

        @NotBlank(message = "Tiêu đề bài học không được để trống")
        @Size(min = 3, max = 255, message = "Tiêu đề phải từ 3 đến 255 ký tự")
        private String title;

        List<MultipartFile> images;

        List<MultipartFile> videos;
    }
}