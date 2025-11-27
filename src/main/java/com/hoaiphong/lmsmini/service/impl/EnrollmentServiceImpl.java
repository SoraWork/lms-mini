package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.response.CourseStudentResponse;
import com.hoaiphong.lmsmini.dto.response.EnrollmentResponse;
import com.hoaiphong.lmsmini.dto.response.ImageResponse;
import com.hoaiphong.lmsmini.dto.response.StudentInCourseResponse;
import com.hoaiphong.lmsmini.entity.*;
import com.hoaiphong.lmsmini.exception.SomeThingWrongException;
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
                .orElseThrow(() -> new SomeThingWrongException("error.student.id.notfound"));

        // Lấy chỉ các course active theo danh sách id yêu cầu
        List<Course> courses = courseRepository.findAllByIdInAndActiveStatus(courseIds);

        // Nếu số lượng khác => có id không tồn tại hoặc không active
        if (courses.size() != (courseIds == null ? 0 : courseIds.size())) {
            throw new SomeThingWrongException("error.course.code.notfound");
        }
        // 3. Check enrollment tồn tại CHỈ 1 QUERY
        List<Long> enrolledCourseIds =
                enrollmentRepository.findEnrolledCourseIds(studentId, courseIds);

        if (!enrolledCourseIds.isEmpty()) {
            throw new SomeThingWrongException("error.enrollment.id.exists");
        }

        List<Enrollment> enrollmentsToSave = courses.stream()
                .map(course -> enrollmentMapper.toEntity(student, course, "1"))
                .toList();

        // Kiểm tra đã enroll hay chưa và tạo danh sách enroll
//        List<Enrollment> enrollmentsToSave = new ArrayList<>();
//        for (Course course : courses) {
//            boolean exists = enrollmentRepository
//                    .existsByStudentIdAndCourseIdAndActiveStatus(studentId, course.getId());
//
//            if (exists) {
//                throw new SomeThingWrongException("error.enrollment.id.exists");
//            }
//
//            Enrollment e = enrollmentMapper.toEntity(student, course, "1");
//
//            enrollmentsToSave.add(e);
//        }
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
        // 0) Validate student
        Student student = studentRepository.findByIdAndActiveStatus(studentId)
                .orElseThrow(() -> new SomeThingWrongException("error.student.id.notfound"));

        List<EnrollmentResponse> results = new ArrayList<>();

         // DELETE (BATCH)
        if (courseIdsDelete != null && !courseIdsDelete.isEmpty()) {

            List<Course> coursesToDelete =
                    courseRepository.findAllByIdInAndActiveStatus(courseIdsDelete);

            if (coursesToDelete.size() != courseIdsDelete.size()) {
                throw new SomeThingWrongException("error.course.code.notfound");
            }

            //1 query lấy toàn bộ enroll cần delete
            List<Enrollment> enrollmentsToDelete =
                    enrollmentRepository.findByStudentAndCourseIds(studentId, courseIdsDelete);

            for (Enrollment e : enrollmentsToDelete) {
                if ("1".equals(e.getStatus())) {
                    e.setStatus("0");
                }
            }

            // save batch
            enrollmentRepository.saveAll(enrollmentsToDelete);

            for (Enrollment e : enrollmentsToDelete) {
                results.add(new EnrollmentResponse(
                        studentId,
                        e.getCourse().getId(),
                        e.getStatus(),
                        e.getCreatedAt(),
                        e.getUpdatedAt()
                ));
            }
        }

        //  CREATE / RE-ACTIVATE (BATCH)
        if (courseIdsCreate != null && !courseIdsCreate.isEmpty()) {

            //  Validate course tồn tại
            List<Course> coursesToCreate =
                    courseRepository.findAllByIdInAndActiveStatus(courseIdsCreate);

            if (coursesToCreate.size() != courseIdsCreate.size()) {
                throw new SomeThingWrongException("error.course.code.notfound");
            }

            //  Lấy toàn bộ enrollment đã tồn tại
            List<Enrollment> existingEnrollments =
                    enrollmentRepository.findByStudentAndCourseIds(studentId, courseIdsCreate);

            Map<Long, Enrollment> enrollmentMap = existingEnrollments.stream()
                    .collect(Collectors.toMap(e -> e.getCourse().getId(), e -> e));

            List<Enrollment> enrollmentsToSave = new ArrayList<>();

            for (Long courseId : courseIdsCreate) {

                Enrollment existing = enrollmentMap.get(courseId);

                if (existing != null) {
                    //  Reactivate
                    if ("0".equals(existing.getStatus())) {
                        existing.setStatus("1");
                    }
                    enrollmentsToSave.add(existing);
                } else {
                    //  Tạo mới
                    Enrollment newEnrollment = new Enrollment();
                    newEnrollment.setId(new EnrollmentId(studentId, courseId));
                    newEnrollment.setStatus("1");
                    newEnrollment.setStudent(student);
                    newEnrollment.setCourse(courseRepository.getReferenceById(courseId));

                    enrollmentsToSave.add(newEnrollment);
                }
            }

            //  save batch
            enrollmentRepository.saveAll(enrollmentsToSave);

            for (Enrollment e : enrollmentsToSave) {
                results.add(new EnrollmentResponse(
                        studentId,
                        e.getCourse().getId(),
                        e.getStatus(),
                        e.getCreatedAt(),
                        e.getUpdatedAt()
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
                .orElseThrow(() -> new SomeThingWrongException("error.course.id.notfound"));

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
