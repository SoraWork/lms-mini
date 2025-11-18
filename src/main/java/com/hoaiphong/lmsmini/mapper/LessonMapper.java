package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.request.LessonCreateRequest;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import com.hoaiphong.lmsmini.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    Lesson toEntity(LessonCreateRequest request);

    @Mapping(target = "courseId", expression = "java(lesson.getCourse().getId())")
    @Mapping(target = "imageIds", ignore = true)
    LessonResponse toResponse(Lesson lesson);
}
