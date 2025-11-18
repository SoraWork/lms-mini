package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.StudentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.StudentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseSummaryResponse;
import com.hoaiphong.lmsmini.dto.response.ImageResponse;
import com.hoaiphong.lmsmini.dto.response.StudentCreateResponse;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.Image;
import com.hoaiphong.lmsmini.entity.Student;
import com.hoaiphong.lmsmini.mapper.ImageMapper;
import com.hoaiphong.lmsmini.mapper.StudentMapper;
import com.hoaiphong.lmsmini.repository.EnrollmentRepository;
import com.hoaiphong.lmsmini.repository.ImageRepository;
import com.hoaiphong.lmsmini.repository.StudentRepository;
import com.hoaiphong.lmsmini.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public CreateResponse<StudentCreateResponse> createStudent(StudentCreateRequest request, List<MultipartFile> images) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        //Tạo Student
        Student student = studentMapper.toEntity(request);
        student = studentRepository.save(student);

        //Upload và tạo Image
        List<Image> savedImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = fileStorageServiceImpl.save(file);
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(student.getId());
                img.setStatus("1");
                savedImages.add(img);
            }
            savedImages = imageRepository.saveAll(savedImages);

            // Cập nhật imageIds trong student
            String imageIds = savedImages.stream()
                    .map(img -> img.getId().toString())
                    .collect(Collectors.joining(","));
            student.setImageIds(imageIds);
            studentRepository.save(student);
        }

        // Trả về id của student
        StudentCreateResponse response = new StudentCreateResponse(student.getId());

        return new CreateResponse<>(200, "student.create.success", response);
    }

    @Override
    public PageResponse<StudentResponse> searchStudents(String name, String email, int page, int size) {
        name = escapeLike(name);
        email = escapeLike(email);

        // Lấy page students
        Page<Student> studentsPage = studentRepository.searchStudents(name, email, PageRequest.of(page, size));
        List<Student> students = studentsPage.getContent();

        if (students.isEmpty()) {
            return new PageResponse<>(List.of(), new PageResponse.Pagination(
                    studentsPage.getNumber(),
                    studentsPage.getSize(),
                    studentsPage.getTotalElements(),
                    studentsPage.getTotalPages(),
                    studentsPage.hasNext(),
                    studentsPage.hasPrevious()
            ));
        }

        List<Long> studentIds = students.stream().map(Student::getId).toList();

        // Lấy ảnh của tất cả students
        List<Image> images = imageRepository.findByObjectIdsAndStatus(studentIds);
        Map<Long, List<Image>> imagesMap = images.stream()
                .collect(Collectors.groupingBy(Image::getObjectId));

        // Lấy enrollments + course
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(studentIds);
        Map<Long, List<Enrollment>> enrollmentsMap = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getStudent().getId()));

        // Map từng student → StudentResponse
        List<StudentResponse> studentResponses = students.stream().map(student -> {
            List<Image> studentImages = imagesMap.getOrDefault(student.getId(), List.of());
            List<Enrollment> studentEnrollments = enrollmentsMap.getOrDefault(student.getId(), List.of());

            StudentResponse response = studentMapper.toResponse(
                    student,
                    studentImages,
                    studentEnrollments.size(),
                    imageMapper
            );

            // Map courses summary
            response.setCourses(studentEnrollments.stream()
                    .map(e -> new CourseSummaryResponse(
                            e.getCourse().getId(),
                            e.getCourse().getName(),
                            e.getCourse().getCode()
                    ))
                    .toList()
            );

            return response;
        }).toList();

        // Trả về PageResponse
        return new PageResponse<>(
                studentResponses,
                new PageResponse.Pagination(
                        studentsPage.getNumber(),
                        studentsPage.getSize(),
                        studentsPage.getTotalElements(),
                        studentsPage.getTotalPages(),
                        studentsPage.hasNext(),
                        studentsPage.hasPrevious()
                )
        );
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request, List<MultipartFile> images) {

        // Lấy student theo id và status = '1'
        Student student = studentRepository.findByIdAndActiveStatus(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Kiểm tra email trùng (khác student hiện tại)
        if(studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Update thông tin cơ bản
        studentMapper.updateStudent(student, request);

        // Xử lý xóa ảnh nếu có deleteImageIds (soft delete)
        if(request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllById(request.getDeleteImageIds());
            imagesToDelete.forEach(img -> img.setStatus("0")); // mark as inactive
            imageRepository.saveAll(imagesToDelete);
        }

        // Thêm ảnh mới nếu có
        if (images != null && !images.isEmpty()) {
            List<Image> newImages = new ArrayList<>();
            for (MultipartFile file : images) {
                String url = fileStorageServiceImpl.save(file); // lưu file
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(student.getId());
                img.setStatus("1");
                newImages.add(img);
            }
            imageRepository.saveAll(newImages);
        }

        // Lưu student
        student = studentRepository.save(student);

        // Lấy danh sách ảnh status = 1 để map sang response
        List<Image> activeImages = imageRepository.findAllById(
                Arrays.stream(Optional.ofNullable(student.getImageIds()).orElse("").split(","))
                        .filter(s -> !s.isBlank())
                        .map(Long::parseLong)
                        .toList()
        ).stream().filter(img -> "1".equals(img.getStatus())).toList();

        // Mapping trả về bằng MapStruct
        Integer totalEnrollments = student.getEnrollments() != null ? student.getEnrollments().size() : 0;
        return studentMapper.toResponse(student, activeImages, totalEnrollments, imageMapper);
    }

    @Override
    public boolean deleteStudent(Long id) {
        Student student = studentRepository.findByIdAndActiveStatus(id).
                orElseThrow(() -> new RuntimeException("Student not found"));
        student.setStatus("0");
        studentRepository.save(student);
        return true;
    }


    public static String escapeLike(String param) {
        if (param == null) return null;
        return param.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");
    }
}
