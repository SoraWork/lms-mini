package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseDetailResponse;
import com.hoaiphong.lmsmini.dto.response.CourseResponse;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
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

    // ENTITY → RESPONSE
    @Mapping(target = "images", expression = "java(imageMapper.toResponseList(images))")
    @Mapping(target = "totalEnrollments", expression = "java(totalEnrollments != null ? totalEnrollments : 0)")
    @Mapping(target = "totalLessons", expression = "java(totalLessons != null ? totalLessons : 0)")
    CourseResponse toResponse(Course course,
                              List<Image> images,
                              Integer totalEnrollments,
                              Integer totalLessons,
                              @Context ImageMapper imageMapper);

    @Mapping(target = "images", expression = "java(imageMapper.toResponseList(images))")
    @Mapping(target = "lessons", expression = "java(lessons != null ? lessons : java.util.Collections.emptyList())")
    CourseDetailResponse toDetailResponse(
            Course course,
            List<Image> images,
            List<LessonResponse> lessons,
            List<StudentResponse> students,
            @Context ImageMapper imageMapper
    );
}
