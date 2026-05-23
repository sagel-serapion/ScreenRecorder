package com.project.screenrecorder.Mapper;


import com.project.screenrecorder.DTO.UploadInitRequest;

import com.project.screenrecorder.Entity.Video;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    UploadInitRequest toDto(Video video);

}
