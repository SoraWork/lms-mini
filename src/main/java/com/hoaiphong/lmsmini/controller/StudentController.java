package com.hoaiphong.lmsmini.controller;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.StudentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.StudentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.StudentCreateResponse;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
import com.hoaiphong.lmsmini.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public CreateResponse<StudentCreateResponse> createStudent(
            @Validated StudentCreateRequest request
    ) {
        return studentService.createStudent(request);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<StudentResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    ){
        PageResponse<StudentResponse> response = studentService.searchStudents(name, email, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @ModelAttribute StudentUpdateRequest request
    ){
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id){
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCategories() throws IOException {
        ByteArrayInputStream in = studentService.exportStudentsActive();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
