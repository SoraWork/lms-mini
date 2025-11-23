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

            // Map entity
            Lesson lesson = lessonMapper.toEntity(item);
            lesson.setCourse(course);

            lesson = lessonRepository.save(lesson);
            Long lessonId = lesson.getId();

            List<Image> savedFiles = new ArrayList<>();

            // ẢNH
            List<String> imageUrls = item.getImages();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<Image> images = imageUrls.stream()
                        .map(url -> {
                            Image img = new Image();
                            img.setUrl(url);
                            img.setType("IMAGE");
                            img.setObjectId(lessonId);
                            img.setStatus("1");
                            return img;
                        })
                        .toList();

                savedFiles.addAll(images);
            }

            // VIDEO
            List<String> videoUrls = item.getVideos();
            if (videoUrls != null && !videoUrls.isEmpty()) {
                List<Image> videos = videoUrls.stream()
                        .map(url -> {
                            Image vid = new Image();
                            vid.setUrl(url);
                            vid.setType("VID");
                            vid.setObjectId(lessonId);
                            vid.setStatus("1");
                            return vid;
                        })
                        .toList();

                savedFiles.addAll(videos);
            }

            // LƯU FILES VÀ GÁN ID VÀO LESSON
            if (!savedFiles.isEmpty()) {
                savedFiles = imageRepository.saveAll(savedFiles);

                String fileIds = savedFiles.stream()
                        .map(f -> f.getId().toString())
                        .collect(Collectors.joining(","));

                lesson.setImageIds(fileIds);
                lessonRepository.save(lesson);
            }

            lessonIds.add(lessonId);
        }

        return new CreateResponse<>(200, "lesson.create.success", new LessonCreateResponse(lessonIds));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(
            Long id,
            LessonUpdateRequest request
    ) {

        // 1. Lấy lesson đang active
        Lesson lesson = lessonRepository.findLessonByIdAndActiveStatus(id)
                .orElseThrow(() -> new SomeThingWrongException("error.lesson.id.notfound"));

        // 2. Update title
        lessonMapper.updateLesson(lesson, request);

        // 3. Parse danh sách ID hiện tại
        List<Long> currentIds = new ArrayList<>();

        if (lesson.getImageIds() != null && !lesson.getImageIds().isBlank()) {
            currentIds = Arrays.stream(lesson.getImageIds().split(","))
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        // 4. DISABLE IMAGE + VIDEO

        List<Long> disableImages = request.getImageIds() != null ? request.getImageIds() : List.of();
        List<Long> disableVideos = request.getVideoIds() != null ? request.getVideoIds() : List.of();

        // Disable IMAGE
        if (!disableImages.isEmpty()) {
            List<Image> imgs = imageRepository.findAllById(disableImages);
            for (Image i : imgs) {
                if (!"IMAGE".equals(i.getType())) {
                    throw new SomeThingWrongException("error.file.type");
                }
                i.setStatus("0");
            }
            imageRepository.saveAll(imgs);
        }

        // Disable VIDEO
        if (!disableVideos.isEmpty()) {
            List<Image> vids = imageRepository.findAllById(disableVideos);
            for (Image v : vids) {
                if (!"VID".equals(v.getType())) {
                    throw new SomeThingWrongException("error.file.type");
                }
                v.setStatus("0");
            }
            imageRepository.saveAll(vids);
        }

        // 5. ADD NEW IMAGES / VIDEOS
        List<Image> newFiles = new ArrayList<>();

        // IMAGE mới
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String url : request.getImages()) {
                Image img = new Image();
                img.setUrl(url);
                img.setType("IMAGE");
                img.setObjectId(lesson.getId());
                img.setStatus("1");
                newFiles.add(img);
            }
        }

        // VIDEO mới
        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
            for (String url : request.getVideos()) {
                Image vid = new Image();
                vid.setUrl(url);
                vid.setType("VID");
                vid.setObjectId(lesson.getId());
                vid.setStatus("1");
                newFiles.add(vid);
            }
        }

        // 6. Save file mới và merge ID
        if (!newFiles.isEmpty()) {
            List<Image> saved = imageRepository.saveAll(newFiles);

            List<Long> addedIds = saved.stream()
                    .map(Image::getId)
                    .toList();

            currentIds.addAll(addedIds);
        }

        // 7. Set lại danh sách ID vào lesson
        String merged = currentIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        lesson.setImageIds(merged);

        // 8. Save lesson
        lessonRepository.save(lesson);

        // 9. Load lại file ACTIVE theo imageIds
        LessonResponse res = lessonMapper.toResponse(lesson);

        List<Image> activeFiles = Collections.emptyList();

        if (merged != null && !merged.isBlank()) {
            List<Long> ids = Arrays.stream(merged.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (!ids.isEmpty()) {
                List<Image> found = imageRepository.findAllById(ids);

                Map<Long, Image> activeMap = found.stream()
                        .filter(img -> "1".equals(img.getStatus()))
                        .collect(Collectors.toMap(Image::getId, img -> img));

                activeFiles = ids.stream()
                        .map(activeMap::get)
                        .filter(Objects::nonNull)
                        .toList();
            }
        }

        // Set images/videos vào response
        res.setImages(
                activeFiles.stream()
                        .filter(i -> "IMAGE".equals(i.getType()))
                        .map(imageMapper::toResponse)
                        .collect(Collectors.toList())
        );

        res.setVideos(
                activeFiles.stream()
                        .filter(i -> "VID".equals(i.getType()))
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
