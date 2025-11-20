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

    @NotNull(message = "error.lesson.courseId.null")
    @Positive(message = "error.lesson.courseId.positive")
    private Long courseId;

    @NotNull(message = "error.lesson.list.null")
    @Size(min = 1, message = "error.lesson.list.size")
    @Valid // validate các LessonItem
    private List<LessonItem> lessons;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonItem {

        @NotBlank(message = "error.lesson.item.title.blank")
        @Size(min = 3, max = 255, message = "error.lesson.item.title.size")
        private String title;

        List<MultipartFile> images;

        List<MultipartFile> videos;
    }
}