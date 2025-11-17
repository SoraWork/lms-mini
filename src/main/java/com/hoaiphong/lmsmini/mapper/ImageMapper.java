package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.response.ImageResponse;
import com.hoaiphong.lmsmini.entity.Image;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageResponse toResponse(Image image);

    List<ImageResponse> toResponseList(List<Image> images);
}
