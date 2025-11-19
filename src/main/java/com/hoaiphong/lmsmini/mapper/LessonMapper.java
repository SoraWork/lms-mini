package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.request.LessonCreateRequest;
import com.hoaiphong.lmsmini.dto.request.LessonUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import com.hoaiphong.lmsmini.entity.Lesson;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    Lesson toEntity(LessonCreateRequest.LessonItem item);

    // UPDATE
    @Mapping(target = "imageIds", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLesson(@MappingTarget Lesson lesson, LessonUpdateRequest request);


    @Mapping(target = "courseId", expression = "java(lesson.getCourse() != null ? lesson.getCourse().getId() : null)")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "videos", ignore = true)
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toResponseList(List<Lesson> lessons);
}
