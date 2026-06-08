package com.project.screenrecorder.DTO.upload;


import lombok.Data;

@Data
public class ChunkUploadResponse {

    private int chunkIndex;

    private Boolean received;
}
