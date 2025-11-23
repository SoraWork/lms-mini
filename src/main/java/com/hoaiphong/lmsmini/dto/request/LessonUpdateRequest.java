package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LessonUpdateRequest {

    @NotBlank(message = "error.lesson.item.title.blank")
    @Size(min = 3, max = 255, message = "error.lesson.item.title.size")
    private String title;

    private List<Long> imageIds;

    private List<Long> videoIds;

    private List<String> images;

    private List<String> videos;
}
