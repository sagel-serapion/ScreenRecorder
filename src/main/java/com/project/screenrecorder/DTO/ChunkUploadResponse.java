package com.project.screenrecorder.DTO;


import lombok.Data;

@Data
public class ChunkUploadResponse {

    private int chunkIndex;

    private Boolean received;
}
