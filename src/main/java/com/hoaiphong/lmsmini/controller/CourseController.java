package com.hoaiphong.lmsmini.controller;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseCreateResponse;
import com.hoaiphong.lmsmini.dto.response.CourseResponse;
import com.hoaiphong.lmsmini.service.CourseService;
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

    @PostMapping
    public CreateResponse<CourseCreateResponse> createCourse(
            @Validated CourseCreateRequest courseCreateRequest,
            @RequestParam(required = false, name = "images") List<MultipartFile> images) {
        return courseService.createCourse(courseCreateRequest, images);
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
            @Valid @ModelAttribute CourseUpdateRequest request,
            @RequestParam(required = false) List<MultipartFile> images
    ){
        CourseResponse courseResponse = courseService.updateCourse(id, request, images);
        return ResponseEntity.ok(courseResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}
