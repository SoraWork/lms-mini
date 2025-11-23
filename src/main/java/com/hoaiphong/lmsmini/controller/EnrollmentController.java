package com.hoaiphong.lmsmini.controller;

import com.hoaiphong.lmsmini.dto.request.EnrollmentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.EnrollmentDeleteRequest;
import com.hoaiphong.lmsmini.dto.request.EnrollmentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.EnrollmentResponse;
import com.hoaiphong.lmsmini.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<List<EnrollmentResponse>> enrollStudent(
            @RequestBody @Valid EnrollmentCreateRequest request) {
        List<EnrollmentResponse> enrollmentResponse = enrollmentService.enrollStudent(request.getStudentId(), request.getCourseIdsCreate());
        return ResponseEntity.ok(enrollmentResponse);
    }


    @PutMapping
    public ResponseEntity<List<EnrollmentResponse>> updateEnrollment(
            @RequestBody @Valid EnrollmentUpdateRequest request
    ){
        List<EnrollmentResponse> enrollmentResponse = enrollmentService.updateEnrollment(request.getStudentId(), request.getCourseIdsDelete(), request.getCourseIdsCreate());
        return  ResponseEntity.ok(enrollmentResponse);
    }

    @DeleteMapping
    public ResponseEntity<List<EnrollmentResponse>> deleteEnrollment(
            @RequestBody @Valid EnrollmentDeleteRequest request
    ){
        enrollmentService.deleteEnrollmentByStudent(request.getStudentId());
        return ResponseEntity.ok().build();
    }
}
