package com.hoaiphong.lmsmini.service;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseCreateResponse;
import com.hoaiphong.lmsmini.dto.response.CourseDetailResponse;
import com.hoaiphong.lmsmini.dto.response.CourseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface CourseService {
    CreateResponse<CourseCreateResponse> createCourse(CourseCreateRequest request);

    PageResponse<CourseResponse> searchCourses(
            String name,
            String code,
            int page,
            int size
    );

    CourseResponse updateCourse(Long id, CourseUpdateRequest request);

    boolean deleteCourse(Long id);

    CourseDetailResponse getCourseDetailById(Long id);

    ByteArrayInputStream exportCoursesActive() throws IOException;
}
