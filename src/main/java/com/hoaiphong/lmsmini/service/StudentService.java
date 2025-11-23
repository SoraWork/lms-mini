package com.hoaiphong.lmsmini.service;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.StudentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.StudentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.StudentCreateResponse;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface StudentService {
    CreateResponse<StudentCreateResponse> createStudent(StudentCreateRequest request);

    PageResponse<StudentResponse> searchStudents(
            String name,
            String email,
            int page,
            int size
    );

    StudentResponse updateStudent(Long id, StudentUpdateRequest request);

    boolean deleteStudent(Long id);

    ByteArrayInputStream exportStudentsActive() throws IOException;
}
