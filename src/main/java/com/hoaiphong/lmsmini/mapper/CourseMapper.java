package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseResponse;
import com.hoaiphong.lmsmini.entity.Course;
import com.hoaiphong.lmsmini.entity.Image;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface CourseMapper {

    // CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Course toEntity(CourseCreateRequest request);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourse(@MappingTarget Course course, CourseUpdateRequest request);

    // ENTITY → RESPONSE (DỮ LIỆU PRELOAD TỪ SERVICE)
    @Mapping(target = "images", expression = "java(imageMapper.toResponseList(images))")
    @Mapping(target = "totalEnrollments", expression = "java(totalEnrollments)")
    @Mapping(target = "totalLessons", expression = "java(totalLessons)")
    CourseResponse toResponse(Course course,
                              List<Image> images,
                              Integer totalEnrollments,
                              Integer totalLessons,
                              @Context ImageMapper imageMapper);
}
