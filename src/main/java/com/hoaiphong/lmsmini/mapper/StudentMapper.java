package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.request.StudentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.StudentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
import com.hoaiphong.lmsmini.entity.Image;
import com.hoaiphong.lmsmini.entity.Student;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface StudentMapper {

    // CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Student toEntity(StudentCreateRequest request);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStudent(@MappingTarget Student student, StudentUpdateRequest request);

    // ENTITY → RESPONSE
    @Mapping(target = "images", expression = "java(imageMapper.toResponseList(images))")
    @Mapping(target = "totalEnrollments", expression = "java(totalEnrollments)")
    @Mapping(target = "courses", ignore = true)
    StudentResponse toResponse(Student student,
                               List<Image> images,
                               Integer totalEnrollments,
                               @Context ImageMapper imageMapper);
}
