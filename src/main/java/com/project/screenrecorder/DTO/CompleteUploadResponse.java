package com.project.screenrecorder.DTO;


import lombok.Data;

@Data
public class CompleteUploadResponse {

    private String token;

    private String shareUrl;

    private String status;
}
