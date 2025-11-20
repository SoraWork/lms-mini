package com.hoaiphong.lmsmini.controller;

import com.hoaiphong.lmsmini.base.CreateResponse;
import com.hoaiphong.lmsmini.dto.request.LessonCreateRequest;
import com.hoaiphong.lmsmini.dto.request.LessonUpdateRequest;
import com.hoaiphong.lmsmini.dto.response.LessonCreateResponse;
import com.hoaiphong.lmsmini.dto.response.LessonResponse;
import com.hoaiphong.lmsmini.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    @PostMapping()
    public CreateResponse<LessonCreateResponse> createLesson(
            @Valid  LessonCreateRequest lessonCreateRequest
    ) {
        return lessonService.createLesson(lessonCreateRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid LessonUpdateRequest request,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) List<MultipartFile> videos
    ){
        LessonResponse lessonResponse = lessonService.updateLesson(id, request, images, videos);
        return ResponseEntity.ok(lessonResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id){
        lessonService.deleteLesson(id);
        return ResponseEntity.ok().build();
    }



}
