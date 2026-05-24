package com.project.screenrecorder.Mapper;


import com.project.screenrecorder.DTO.UploadInitRequest;

import com.project.screenrecorder.Entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoMapper {




     Video toEntity(UploadInitRequest request);

}
