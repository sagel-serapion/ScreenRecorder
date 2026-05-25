package com.project.screenrecorder.Controller;



import com.project.screenrecorder.DTO.UploadInitRequest;
import com.project.screenrecorder.DTO.UploadInitResponse;
import com.project.screenrecorder.Service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

        private final UploadService uploadService;


        @PostMapping("/init")
        ResponseEntity<UploadInitResponse> uploadInit(@Valid @RequestBody UploadInitRequest uploadInitRequest){
            UploadInitResponse response = uploadService.initUpload(uploadInitRequest);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }



}
