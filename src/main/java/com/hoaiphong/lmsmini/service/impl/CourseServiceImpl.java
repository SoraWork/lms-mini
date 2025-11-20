package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.CountDTO;
import com.hoaiphong.lmsmini.dto.request.CourseCreateRequest;
import com.hoaiphong.lmsmini.dto.request.CourseUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.*;
import com.hoaiphong.lmsmini.entity.Course;
import com.hoaiphong.lmsmini.entity.Image;
import com.hoaiphong.lmsmini.entity.Lesson;
import com.hoaiphong.lmsmini.exception.SomeThingWrongException;
import com.hoaiphong.lmsmini.mapper.CourseMapper;
import com.hoaiphong.lmsmini.mapper.ImageMapper;
import com.hoaiphong.lmsmini.mapper.LessonMapper;
import com.hoaiphong.lmsmini.mapper.VidMapper;
import com.hoaiphong.lmsmini.repository.CourseRepository;
import com.hoaiphong.lmsmini.repository.EnrollmentRepository;
import com.hoaiphong.lmsmini.repository.ImageRepository;
import com.hoaiphong.lmsmini.repository.LessonRepository;
import com.hoaiphong.lmsmini.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.hoaiphong.lmsmini.service.impl.StudentServiceImpl.escapeLike;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final VidMapper vidMapper;

    @Override
    public CreateResponse<CourseCreateResponse> createCourse(CourseCreateRequest request, List<MultipartFile> images) {
        if ( courseRepository.existsByCode(request.getCode()) ) {
            throw new SomeThingWrongException("error.course.code.exists");
        }

        Course course = courseMapper.toEntity(request);
        course = courseRepository.save(course);
        //Upload và tạo Image
        List<Image> savedImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = fileStorageServiceImpl.save(file);
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");//thumbnail cua no
                img.setObjectId(course.getId());
                img.setStatus("1");
                savedImages.add(img);
            }
            savedImages = imageRepository.saveAll(savedImages);

            // Cập nhật imageIds trong course
            String imageIds = savedImages.stream()
                    .map(img -> img.getId().toString())
                    .collect(Collectors.joining(","));
            course.setImageIds(imageIds);
            courseRepository.save(course);
        }
        CourseCreateResponse response = new CourseCreateResponse(course.getId());

        return new CreateResponse<>(200, "course.create.success", response);
    }

    @Override
    public PageResponse<CourseResponse> searchCourses(String name, String code, int page, int size) {
        name = escapeLike(name);
        code = escapeLike(code);

        Pageable pageable = PageRequest.of(page, size);

        //Lấy dữ liệu page hiện tại
        List<Course> courses = courseRepository.searchCoursesList(name, code, pageable);

        // COUNT chính xác
        Long total = courseRepository.countCourses(name, code);

        // Tính toán pagination giống Page
        int totalPages = (int) Math.ceil((double) total / size);
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0;

        if (courses.isEmpty()) {
            return new PageResponse<>(List.of(), new PageResponse.Pagination(
                    page,
                    size,
                    total,
                    totalPages,
                    hasNext,
                    hasPrevious
            ));
        }

        // Tập hợp tất cả imageIds từ courses
        List<Long> allImageIds = courses.stream()
                .flatMap(c -> Arrays.stream(Optional.ofNullable(c.getImageIds()).orElse("").split(",")))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        // Lấy tất cả images active
        List<Image> allImages = allImageIds.isEmpty() ? List.of() : imageRepository.findByIds(allImageIds)
                .stream().filter(img -> "1".equals(img.getStatus())).toList();

        //  Lấy enrollments và lessons count
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Long> enrollmentsMap = enrollmentRepository.countByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(CountDTO::getCourseId, CountDTO::getCount));
        Map<Long, Long> lessonsMap = lessonRepository.countByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(CountDTO::getCourseId, CountDTO::getCount));

        // Map Course → CourseResponse
        List<CourseResponse> courseResponses = courses.stream().map(course -> {
            List<Image> courseImgs = Arrays.stream(Optional.ofNullable(course.getImageIds()).orElse("").split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .flatMap(id -> allImages.stream().filter(img -> img.getId().equals(id)))
                    .toList();

            return courseMapper.toResponse(
                    course,
                    courseImgs,
                    enrollmentsMap.getOrDefault(course.getId(), 0L).intValue(),
                    lessonsMap.getOrDefault(course.getId(), 0L).intValue(),
                    imageMapper
            );
        }).toList();

        return new PageResponse<>(courseResponses, new PageResponse.Pagination(
                page,
                size,
                total,
                totalPages,
                hasNext,
                hasPrevious
        ));
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request, List<MultipartFile> images) {
        // lấy course theo id và status = 1
        Course course = courseRepository.findCourseByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.course.id.notfound"));

        // Kiểm tra code khóa học có trùng k
        if (courseRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new SomeThingWrongException("error.course.code.exists");
        }
        //Update thong tin co ban
        courseMapper.updateCourse(course, request);

        List<Long> currentImageIds = Arrays.stream(
                        Optional.ofNullable(course.getImageIds()).orElse("")
                                .split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Xu ly xoa anh neu co deleteImageIds
        if(request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            List<Image> imagesToDelete = imageRepository.findAllById(request.getDeleteImageIds());
            imagesToDelete.forEach(img -> img.setStatus("0")); // mark as inactive
            imageRepository.saveAll(imagesToDelete);
        }

        // them anh moi
        if (images != null && !images.isEmpty()) {
            List<Image> newImages = new ArrayList<>();
            for (MultipartFile file : images) {
                String url = fileStorageServiceImpl.save(file); // lưu file
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(course.getId());
                img.setStatus("1");
                newImages.add(img);
            }
            newImages = imageRepository.saveAll(newImages);
            // Thêm id ảnh mới vào list
            currentImageIds.addAll(newImages.stream().map(Image::getId).toList());
        }

        //cap nhat lại list imgids
        if (!currentImageIds.isEmpty()) {
            course.setImageIds(currentImageIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        // luu course
        course = courseRepository.save(course);

        // lấy danh sách ảnh có status =1
        List<Image> activeImages = imageRepository.findAllById(
                Arrays.stream(Optional.ofNullable(course.getImageIds()).orElse("").split(","))
                        .filter(s -> !s.isBlank())
                        .map(Long::parseLong)
                        .toList()
        ).stream().filter(img -> "1".equals(img.getStatus())).toList();

        // Preload tổng số enrollments và lessons
        List<CountDTO> enrollmentsCountList = enrollmentRepository.countByCourseIds(List.of(course.getId()));
        int totalEnrollments = enrollmentsCountList.isEmpty() ? 0 : enrollmentsCountList.getFirst().getCount().intValue();
        List<CountDTO> lessonsCountList = lessonRepository.countByCourseIds(List.of(course.getId()));
        int totalLessons = lessonsCountList.isEmpty() ? 0 : lessonsCountList.getFirst().getCount().intValue();
        return courseMapper.toResponse(
                course,
                activeImages,
                totalEnrollments,
                totalLessons,
                imageMapper
        );
    }

    @Override
    public boolean deleteCourse(Long id) {
        Course course = courseRepository.findCourseByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.course.id.notfound"));
        course.setStatus("0");
        courseRepository.save(course);
        return true;
    }

    @Override
    public CourseDetailResponse getCourseDetailById(Long id) {
        // Lấy course active
        Course course = courseRepository.findByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.course.id.notfound"));

        //  Lấy tất cả imageIds của course
        List<Long> courseImageIds = Arrays.stream(Optional.ofNullable(course.getImageIds()).orElse("").split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        // Lấy images của course
        List<Image> courseImages = courseImageIds.isEmpty() ? List.of() : imageRepository.findByIds(courseImageIds)
                .stream().filter(img -> "1".equals(img.getStatus())).toList();

        // Lấy lesson active của course
        List<Lesson> lessons = lessonRepository.findByCourseIdActive(course.getId());

        // Lấy tất cả image/video của lessons
        List<Long> allLessonImageIds = lessons.stream()
                .flatMap(lesson -> Arrays.stream(Optional.ofNullable(lesson.getImageIds()).orElse("").split(",")))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        // lọc ảnh nào có status = 1
        List<Image> lessonImages = allLessonImageIds.isEmpty() ? List.of() : imageRepository.findByIds(allLessonImageIds)
                .stream().filter(img -> "1".equals(img.getStatus())).toList();

        System.out.println(lessonImages);
        // Tạo map id -> image để lookup nhanh
        Map<Long, Image> lessonImageMap = lessonImages.stream()
                .collect(Collectors.toMap(Image::getId, img -> img));

        System.out.println(lessonImageMap);
        //  Map lesson → LessonResponse
        List<LessonResponse> lessonResponses = lessons.stream().map(lesson -> {
            LessonResponse lr = lessonMapper.toResponse(lesson);

            List<Long> ids = Arrays.stream(Optional.ofNullable(lesson.getImageIds()).orElse("").split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .toList();

            List<Image> imgs = ids.stream()
                    .map(lessonImageMap::get)
                    .filter(Objects::nonNull)
                    .filter(img -> "IMAGE".equals(img.getType()))
                    .toList();

            List<Image> vids = ids.stream()
                    .map(lessonImageMap::get)
                    .filter(Objects::nonNull)
                    .filter(img -> "VID".equals(img.getType()))
                    .toList();

            lr.setImages(imageMapper.toResponseList(imgs));
            lr.setVideos(vidMapper.toResponseList(vids));
            return lr;
        }).toList();

        //  Map sang CourseDetailResponse
        return courseMapper.toDetailResponse(
                course,
                courseImages,
                lessonResponses,
                null,
                imageMapper
        );
    }
}
