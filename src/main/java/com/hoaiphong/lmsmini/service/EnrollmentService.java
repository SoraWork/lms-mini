package com.hoaiphong.lmsmini.service;

import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.response.CourseStudentResponse;
import com.hoaiphong.lmsmini.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    List<EnrollmentResponse> enrollStudent(Long studentId, List<Long> courseIds);

    List<EnrollmentResponse> updateEnrollment(Long studentId, List<Long> courseIdsDelete,List<Long> courseIdsCreate);

    int deleteEnrollmentByStudent(Long studentId);

    int deleteEnrollmentByCourse(Long courseId);

    CourseStudentResponse getStudentsOfCourse(Long courseId, int page, int size);
}
