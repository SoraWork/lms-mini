package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.response.EnrollmentResponse;
import com.hoaiphong.lmsmini.entity.Course;
import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.EnrollmentId;
import com.hoaiphong.lmsmini.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    EnrollmentMapper INSTANCE = Mappers.getMapper(EnrollmentMapper.class);

    @Mapping(target = "studentId", source = "id.studentId")
    @Mapping(target = "courseId", source = "id.courseId")
    EnrollmentResponse toResponse(Enrollment enrollment);

    default Enrollment toEntity(Student student, Course course, String status) {
        EnrollmentId id = new EnrollmentId(student.getId(), course.getId()); // Tạo EnrollmentId
        Enrollment enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(status);
        return enrollment;
    }
}
