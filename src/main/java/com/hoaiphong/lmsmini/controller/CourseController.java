package com.hoaiphong.lmsmini.controller;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseCreateResponse;
import com.hoaiphong.lmsmini.dto.response.CourseDetailResponse;
import com.hoaiphong.lmsmini.dto.response.CourseResponse;
import com.hoaiphong.lmsmini.dto.response.CourseStudentResponse;
import com.hoaiphong.lmsmini.service.CourseService;
import com.hoaiphong.lmsmini.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/{id}/lesson")
    public ResponseEntity<CourseDetailResponse> getCourseDetailById(@PathVariable Long id) {
        CourseDetailResponse response = courseService.getCourseDetailById(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/student")
    public ResponseEntity<CourseStudentResponse> getStudentInCourseById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CourseStudentResponse response = enrollmentService.getStudentsOfCourse(id, page, size);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public CreateResponse<CourseCreateResponse> createCourse(
            @Validated CourseCreateRequest courseCreateRequest) {
        return courseService.createCourse(courseCreateRequest);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<CourseResponse>> searchCourse(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    ){
        PageResponse<CourseResponse> response = courseService.searchCourses(name, code, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @ModelAttribute CourseUpdateRequest request
    ){
        CourseResponse courseResponse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(courseResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}
