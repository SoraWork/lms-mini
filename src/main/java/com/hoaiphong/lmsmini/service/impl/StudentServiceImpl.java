package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.StudentCreateRequest;
import com.hoaiphong.lmsmini.dto.request.StudentUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.CourseSummaryResponse;
import com.hoaiphong.lmsmini.dto.response.ImageResponse;
import com.hoaiphong.lmsmini.dto.response.StudentCreateResponse;
import com.hoaiphong.lmsmini.dto.response.StudentResponse;
import com.hoaiphong.lmsmini.entity.Course;
import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.Image;
import com.hoaiphong.lmsmini.entity.Student;
import com.hoaiphong.lmsmini.exception.SomeThingWrongException;
import com.hoaiphong.lmsmini.mapper.ImageMapper;
import com.hoaiphong.lmsmini.mapper.StudentMapper;
import com.hoaiphong.lmsmini.repository.EnrollmentRepository;
import com.hoaiphong.lmsmini.repository.ImageRepository;
import com.hoaiphong.lmsmini.repository.StudentRepository;
import com.hoaiphong.lmsmini.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public CreateResponse<StudentCreateResponse> createStudent(StudentCreateRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new SomeThingWrongException("error.student.email.exists");
        }

        // 1) Tạo student
        Student student = studentMapper.toEntity(request);
        student = studentRepository.save(student);

        //lấy id từ student vừa tạo
        Long studentId = student.getId();
        //lấy ảnh vừa tạo bên FE
        List<String> imageUrls = request.getImages();

        // 2) Lưu images từ URL FE gửi lên
        if (imageUrls != null && !imageUrls.isEmpty()) {

            List<Image> savedImages = imageUrls.stream().map(url -> {
                Image img = new Image();
                img.setUrl(url);              // FE đã upload lên Cloudinary trước
                img.setType("IMAGE");
                img.setObjectId(studentId);
                img.setStatus("1");
                return img;
            }).collect(Collectors.toList());

            savedImages = imageRepository.saveAll(savedImages);

            // 3) Lưu imageIds vào Student
            String imageIds = savedImages.stream()
                    .map(img -> img.getId().toString())
                    .collect(Collectors.joining(","));

            student.setImageIds(imageIds);
            studentRepository.save(student);
        }

        StudentCreateResponse response = new StudentCreateResponse(student.getId());
        return new CreateResponse<>(200, "student.create.success", response);
    }

    @Override
    public PageResponse<StudentResponse> searchStudents(String name, String email, int page, int size) {
        name = escapeLike(name);
        email = escapeLike(email);

        Pageable pageable = PageRequest.of(page, size);
        //Lấy dữ liệu page hiện tại
        List<Student> students = studentRepository.searchStudentsList(name, email, pageable);

        // COUNT chính xác
        Long total = studentRepository.countStudents(name, email);

        // Tính toán pagination giống Page
        int totalPages = (int) Math.ceil((double) total / size);
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0;

        if (students.isEmpty()) {
            return new PageResponse<>(List.of(), new PageResponse.Pagination(
                    page,
                    size,
                    total,
                    totalPages,
                    hasNext,
                    hasPrevious
            ));
        }

        //Tập hợp tất cả imageIds từ students
        List<Long> allImageIds = students.stream()
                .flatMap(s -> Arrays.stream(Optional.ofNullable(s.getImageIds()).orElse("").split(",")))
                .filter(str -> !str.isBlank())
                .map(Long::valueOf)
                .toList();

        // Lấy tất cả ảnh/video theo ids
        List<Image> allImages = allImageIds.isEmpty() ? List.of() : imageRepository.findByIds(allImageIds);

        // Lấy enrollments + course
        List<Long> studentIds = students.stream().map(Student::getId).toList();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdIn(studentIds);
        Map<Long, List<Enrollment>> enrollmentsMap = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getStudent().getId()));

        //  Map từng student → StudentResponse
        List<StudentResponse> studentResponses = students.stream().map(student -> {
            List<Long> studentImageIds = Arrays.stream(Optional.ofNullable(student.getImageIds()).orElse("").split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();

            List<Image> studentImages = allImages.stream()
                    .filter(img -> studentImageIds.contains(img.getId()) && "1".equals(img.getStatus()))
                    .toList();

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

        return new PageResponse<>(
                studentResponses,
                new PageResponse.Pagination(
                        page,
                        size,
                        total,
                        totalPages,
                        hasNext,
                        hasPrevious
                )
        );
    }
    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {

        // Lấy student theo id và status = '1'
        Student student = studentRepository.findByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.student.id.notfound"));

        // Kiểm tra email trùng (khác student hiện tại)
        if(studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new SomeThingWrongException("error.student.email.exists");
        }

        // Update thông tin cơ bản
        studentMapper.updateStudent(student, request);

        //Lay ra list imgid de so sanh
        List<Long> currentImageIds = Arrays.stream(
                        Optional.ofNullable(student.getImageIds()).orElse("")
                                .split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Xử lý xóa ảnh nếu có deleteImageIds (soft delete)
        if(request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllById(request.getDeleteImageIds());
            imagesToDelete.forEach(img -> img.setStatus("0")); // mark as inactive
            imageRepository.saveAll(imagesToDelete);
        }

        //lấy ảnh vừa tạo bên FE
        List<String> imageUrls = request.getImages();

        // Thêm ảnh mới nếu có
        if (imageUrls != null && !imageUrls.isEmpty()) {

            List<Image> savedImages = imageUrls.stream().map(url -> {
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(id);
                img.setStatus("1");
                return img;
            }).collect(Collectors.toList());

            savedImages = imageRepository.saveAll(savedImages);
            // Thêm id ảnh mới vào list
            currentImageIds.addAll(savedImages.stream().map(Image::getId).toList());
        }
        //cap nhat lại list imgids
        if (!currentImageIds.isEmpty()) {
            student.setImageIds(currentImageIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
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
        Student student = studentRepository.findByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.student.id.notfound"));

        Long activeEnrollCount = enrollmentRepository.countActiveByStudentId(id);

        if (activeEnrollCount != null && activeEnrollCount > 0) {
            throw new SomeThingWrongException("error.student.has.enrollments");
        }
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
