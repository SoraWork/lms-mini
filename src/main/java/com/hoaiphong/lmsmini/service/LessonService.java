package com.hoaiphong.lmsmini.service;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.base.PageResponse;
import com.hoaiphong.lmsmini.dto.request.LessonCreateRequest;
import com.hoaiphong.lmsmini.dto.request.LessonUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.LessonCreateResponse;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LessonService {
    CreateResponse<LessonCreateResponse> createLesson(LessonCreateRequest request);


    LessonResponse updateLesson(Long id, LessonUpdateRequest request, List<MultipartFile> images, List<MultipartFile> videos);

    boolean deleteLesson(Long id);
}
