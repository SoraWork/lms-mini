package com.hoaiphong.lmsmini.service.impl;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.LessonCreateRequest;
import com.hoaiphong.lmsmini.dto.request.LessonUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.LessonCreateResponse;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import com.hoaiphong.lmsmini.entity.Image;
import com.hoaiphong.lmsmini.entity.Lesson;
import com.hoaiphong.lmsmini.exception.SomeThingWrongException;
import com.hoaiphong.lmsmini.mapper.ImageMapper;
import com.hoaiphong.lmsmini.mapper.LessonMapper;
import com.hoaiphong.lmsmini.mapper.VidMapper;
import com.hoaiphong.lmsmini.repository.CourseRepository;
import com.hoaiphong.lmsmini.repository.ImageRepository;
import com.hoaiphong.lmsmini.repository.LessonRepository;
import com.hoaiphong.lmsmini.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final CourseRepository courseRepository;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final VidMapper vidMapper;


    @Override
    @Transactional
    public CreateResponse<LessonCreateResponse> createLesson(LessonCreateRequest request) {

        var course = courseRepository.findCourseByIdAndActiveStatus(request.getCourseId())
                .orElseThrow(() -> new SomeThingWrongException("error.course.id.notfound"));

        List<Long> lessonIds = new ArrayList<>();

        for (LessonCreateRequest.LessonItem item : request.getLessons()) {

            // MapStruct map cơ bản
            Lesson lesson = lessonMapper.toEntity(item);
            lesson.setCourse(course);

            lesson = lessonRepository.save(lesson);

            List<Image> savedFiles = new ArrayList<>();

            // Upload images
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                for (MultipartFile file : item.getImages()) {
                    String url = fileStorageServiceImpl.save(file);
                    Image img = new Image();
                    img.setUrl(url);
                    img.setType("IMAGE");
                    img.setObjectId(lesson.getId());
                    img.setStatus("1");
                    savedFiles.add(img);
                }
            }

            // Upload videos
            if (item.getVideos() != null && !item.getVideos().isEmpty()) {
                for (MultipartFile file : item.getVideos()) {
                    String url = fileStorageServiceImpl.save(file);
                    Image vid = new Image();
                    vid.setUrl(url);
                    vid.setType("VID");
                    vid.setObjectId(lesson.getId());
                    vid.setStatus("1");
                    savedFiles.add(vid);
                }
            }

            // Save tất cả files và gán imageIds
            if (!savedFiles.isEmpty()) {
                savedFiles = imageRepository.saveAll(savedFiles);
                String fileIds = savedFiles.stream()
                        .map(f -> f.getId().toString())
                        .collect(Collectors.joining(","));
                lesson.setImageIds(fileIds);
                lessonRepository.save(lesson);
            }

            lessonIds.add(lesson.getId());
        }

        return new CreateResponse<>(200, "lesson.create.success", new LessonCreateResponse(lessonIds));
    }


    @Override
    @Transactional
    public LessonResponse updateLesson(
            Long id,
            LessonUpdateRequest request,
            List<MultipartFile> images,
            List<MultipartFile> videos
    ) {

        // 1. Lấy lesson đang active
        Lesson lesson = lessonRepository.findLessonByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.lesson.id.notfound"));

        // 2. Update title
        lessonMapper.updateLesson(lesson, request);

        // 3. Lấy danh sách ID hiện tại trong lesson.imageIds
        List<Long> currentIds = new ArrayList<>();

        if (lesson.getImageIds() != null && !lesson.getImageIds().isBlank()) {
            currentIds = Arrays.stream(lesson.getImageIds().split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        // 4. XỬ LÝ UPDATE STATUS = 0

        List<Long> disableImages = request.getImageIds() != null ? request.getImageIds() : List.of();
        List<Long> disableVideos = request.getVideoIds() != null ? request.getVideoIds() : List.of();

        // 4.1 Disable IMAGE
        if (!disableImages.isEmpty()) {
            List<Image> imgs = imageRepository.findAllById(disableImages);

            // validate: tất cả phải type=IMAGE
            for (Image i : imgs) {
                if (!"IMAGE".equals(i.getType())) {
                    throw new SomeThingWrongException("error.file.type");
                }
                if (!i.getObjectId().equals(lesson.getId())) {
                    throw new SomeThingWrongException("error.file");
                }
                i.setStatus("0");
            }
            imageRepository.saveAll(imgs);
        }

        // 4.2 Disable VIDEO
        if (!disableVideos.isEmpty()) {
            List<Image> vids = imageRepository.findAllById(disableVideos);

            // validate: tất cả phải VID
            for (Image v : vids) {
                if (!"VID".equals(v.getType())) {
                    throw new SomeThingWrongException("error.file.type");
                }
                if (!v.getObjectId().equals(lesson.getId())) {
                    throw new SomeThingWrongException("error.file");
                }
                v.setStatus("0");
            }
            imageRepository.saveAll(vids);
        }

        // 5. UPLOAD MỚI
        List<Image> newFiles = new ArrayList<>();

        // IMAGE mới
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = fileStorageServiceImpl.save(file);

                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(lesson.getId());
                img.setStatus("1");
                newFiles.add(img);
            }
        }

        // VIDEO mới
        if (videos != null && !videos.isEmpty()) {
            for (MultipartFile file : videos) {
                String url = fileStorageServiceImpl.save(file);

                Image vid = new Image();
                vid.setUrl(url);
                vid.setType("VID");
                vid.setObjectId(lesson.getId());
                vid.setStatus("1");
                newFiles.add(vid);
            }
        }

        // 6. Lưu file mới + append ID
        if (!newFiles.isEmpty()) {
            List<Image> saved = imageRepository.saveAll(newFiles);

            List<Long> addedIds = saved.stream()
                    .map(Image::getId)
                    .toList();

            currentIds.addAll(addedIds);
        }

        // 7. Set lại imageIds (bao gồm cả id đã disable)
        String merged = currentIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        lesson.setImageIds(merged);

        // 8. Save lesson
        lessonRepository.save(lesson);

        // 9. Convert sang response
        LessonResponse res = lessonMapper.toResponse(lesson);

        //  Load lại ảnh và video ACTIVE để trả về (thay thế cho findById(lesson.getId()))
        String idsStr = lesson.getImageIds();
        List<Image> activeFiles = Collections.emptyList();

        if (idsStr != null && !idsStr.isBlank()) {
            // 1. parse string "1,2,3" -> List<Long> ids
            List<Long> ids = Arrays.stream(idsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (!ids.isEmpty()) {
                // 2. lấy tất cả Image theo ids
                List<Image> found = imageRepository.findAllById(ids);

                // 3. giữ lại các file active = "1"
                Map<Long, Image> activeMap = found.stream()
                        .filter(img -> "1".equals(img.getStatus()))
                        .collect(Collectors.toMap(Image::getId, img -> img));

                // 4. giữ đúng order theo ids (bỏ các id không tồn tại / inactive)
                activeFiles = ids.stream()
                        .map(activeMap::get)
                        .filter(Objects::nonNull)
                        .toList();
            }
        }

        // 5. set images / videos vào response theo type
        res.setImages(
                activeFiles.stream()
                        .filter(i -> "IMAGE".equalsIgnoreCase(i.getType()))
                        .map(imageMapper::toResponse)
                        .collect(Collectors.toList())
        );

        res.setVideos(
                activeFiles.stream()
                        .filter(i -> "VID".equalsIgnoreCase(i.getType()))
                        .map(vidMapper::toResponse)
                        .collect(Collectors.toList())
        );

        return res;
    }




    @Override
    public boolean deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findLessonByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.lesson.id.notfound"));
        lesson.setStatus("0");
        lessonRepository.save(lesson);
        return false;
    }
}
