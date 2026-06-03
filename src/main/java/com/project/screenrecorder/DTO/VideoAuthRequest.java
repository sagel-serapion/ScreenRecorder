package com.project.screenrecorder.DTO;


import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoAuthRequest {


    @Size(min = 6, message = "Password must be at least 6 characters")
    private String  password;
}
