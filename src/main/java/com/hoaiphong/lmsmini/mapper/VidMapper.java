package com.hoaiphong.lmsmini.mapper;

import com.hoaiphong.lmsmini.dto.response.VidResponse;
import com.hoaiphong.lmsmini.entity.Image;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VidMapper {

    VidResponse toResponse(Image image);

    List<VidResponse> toResponseList(List<Image> images);
}
