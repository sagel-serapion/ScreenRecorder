package com.project.screenrecorder.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UploadInitRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;


    private boolean hasPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
