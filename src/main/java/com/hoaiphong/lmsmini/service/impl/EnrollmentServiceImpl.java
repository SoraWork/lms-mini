package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.response.CourseStudentResponse;
import com.hoaiphong.lmsmini.dto.response.EnrollmentResponse;
import com.hoaiphong.lmsmini.dto.response.ImageResponse;
import com.hoaiphong.lmsmini.dto.response.StudentInCourseResponse;
import com.hoaiphong.lmsmini.entity.*;
import com.hoaiphong.lmsmini.mapper.*;
import com.hoaiphong.lmsmini.repository.*;
import com.hoaiphong.lmsmini.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final VidMapper vidMapper;
    private final StudentRepository studentRepository;


    @Override
    @Transactional
    public List<EnrollmentResponse> enrollStudent(Long studentId, List<Long> courseIds) {
        // validate student (giữ nguyên phần kiểm tra student của bạn)
        Student student = studentRepository.findByIdAndActiveStatus(studentId)
                .orElseThrow(() -> new RuntimeException("Student không tồn tại hoặc không active"));

        // Lấy chỉ các course active theo danh sách id yêu cầu
        List<Course> courses = courseRepository.findAllByIdInAndActiveStatus(courseIds);

        // Nếu số lượng khác => có id không tồn tại hoặc không active
        if (courses.size() != (courseIds == null ? 0 : courseIds.size())) {
            Set<Long> foundIds = courses.stream().map(Course::getId).collect(Collectors.toSet());
            List<Long> missing = courseIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new RuntimeException("Các course sau không tồn tại hoặc không active: " + missing);
        }

        // Kiểm tra đã enroll hay chưa và tạo danh sách enroll
        List<Enrollment> enrollmentsToSave = new ArrayList<>();
        for (Course course : courses) {
            boolean exists = enrollmentRepository
                    .existsByStudentIdAndCourseIdAndActiveStatus(studentId, course.getId());

            if (exists) {
                throw new RuntimeException("Student đã đăng ký course ID = " + course.getId());
            }

            Enrollment e = enrollmentMapper.toEntity(student, course, "1");

            enrollmentsToSave.add(e);
        }
        List<Enrollment> saved = enrollmentRepository.saveAll(enrollmentsToSave);

        return saved.stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<EnrollmentResponse> updateEnrollment(
            Long studentId,
            List<Long> courseIdsDelete,
            List<Long> courseIdsCreate
    ) {
        List<EnrollmentResponse> results = new ArrayList<>();

        // =======================
        // 1) DELETE nếu có
        // =======================
        if (courseIdsDelete != null && !courseIdsDelete.isEmpty()) {
            for (Long courseId : courseIdsDelete) {
                Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);

                if (enrollment != null && "1".equals(enrollment.getStatus())) {
                    enrollment.setStatus("0");
                    enrollmentRepository.save(enrollment);
                }

                if (enrollment != null) {
                    results.add(new EnrollmentResponse(
                            studentId,
                            courseId,
                            enrollment.getStatus(),
                            enrollment.getCreatedAt(),
                            enrollment.getUpdatedAt()
                    ));
                }
            }
        }

        // =======================
        // 2) CREATE nếu có
        // =======================
        if (courseIdsCreate != null && !courseIdsCreate.isEmpty()) {
            for (Long courseId : courseIdsCreate) {
                Enrollment existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);

                if (existing != null) {
                    if ("0".equals(existing.getStatus())) {
                        // reactivate
                        existing.setStatus("1");
                        enrollmentRepository.save(existing);
                    }
                    // nếu đang active → bỏ qua
                    results.add(new EnrollmentResponse(
                            studentId,
                            courseId,
                            existing.getStatus(),
                            existing.getCreatedAt(),
                            existing.getUpdatedAt()
                    ));
                    continue;
                }

                // chưa tồn tại → tạo mới
                Enrollment newEnrollment = new Enrollment();
                newEnrollment.setId(new EnrollmentId(studentId, courseId));
                newEnrollment.setStatus("1");
                newEnrollment.setStudent(studentRepository.getReferenceById(studentId));
                newEnrollment.setCourse(courseRepository.getReferenceById(courseId));

                enrollmentRepository.save(newEnrollment);

                results.add(new EnrollmentResponse(
                        studentId,
                        courseId,
                        "1",
                        newEnrollment.getCreatedAt(),
                        newEnrollment.getUpdatedAt()
                ));
            }
        }

        return results;
    }


    @Override
    @Transactional
    public int deleteEnrollmentByStudent(Long studentId) {
        return enrollmentRepository.softDeleteByStudentId(studentId);
    }

    @Override
    @Transactional
    public int deleteEnrollmentByCourse(Long courseId) {
        return enrollmentRepository.softDeleteByCourseId(courseId);
    }


    @Override
    public CourseStudentResponse getStudentsOfCourse(Long courseId, int page, int size) {
        Course course = courseRepository.findByIdAndActiveStatus(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Student> studentPage = enrollmentRepository.findActiveStudentsByCourseId(courseId, pageable);

        List<StudentInCourseResponse> studentResponses = studentPage.getContent().stream()
                .map(student -> {
                    StudentInCourseResponse s = new StudentInCourseResponse();
                    s.setId(student.getId());
                    s.setName(student.getName());
                    s.setEmail(student.getEmail());

                    // xử lý imageIds String -> List<Long>
                    List<Long> ids = Arrays.stream(Optional.ofNullable(student.getImageIds())
                                    .orElse("")
                                    .split(","))
                            .filter(str -> !str.isBlank())
                            .map(Long::valueOf)
                            .toList();

                    // query imageRepository
                    List<Image> imgs = ids.isEmpty() ? List.of() : imageRepository.findByIds(ids);

                    s.setImages(imageMapper.toResponseList(imgs));

                    s.setStatus(student.getStatus());
                    s.setCreatedAt(student.getCreatedAt());
                    s.setUpdatedAt(student.getUpdatedAt());
                    return s;
                }).toList();

        // Tạo PageResponse
        PageResponse<StudentInCourseResponse> pageResponse = new PageResponse<>();
        pageResponse.setData(studentResponses);
        pageResponse.setPagination(new PageResponse.Pagination(
                studentPage.getNumber(),
                studentPage.getSize(),
                studentPage.getTotalElements(),
                studentPage.getTotalPages(),
                studentPage.hasNext(),
                studentPage.hasPrevious()
        ));

        // Map sang CourseStudentResponse
        CourseStudentResponse response = new CourseStudentResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setCode(course.getCode());

        // images course
        List<Image> courseImages = Arrays.stream(Optional.ofNullable(course.getImageIds()).orElse("").split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .map(id -> imageRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .filter(img -> "1".equals(img.getStatus()))
                .toList();
        response.setImages(imageMapper.toResponseList(courseImages));

        response.setStudents(pageResponse);
        response.setStatus(course.getStatus());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());

        return response;
    }

}
