package com.project.screenrecorder.Controller;



import com.project.screenrecorder.DTO.upload.ChunkUploadResponse;
import com.project.screenrecorder.DTO.upload.CompleteUploadResponse;
import com.project.screenrecorder.DTO.upload.UploadInitRequest;
import com.project.screenrecorder.DTO.upload.UploadInitResponse;
import com.project.screenrecorder.Service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        @PostMapping("/{videoId}/chunk")
        ResponseEntity<ChunkUploadResponse> chunkUpload(
                @PathVariable String videoId,
                @RequestParam int chunkIndex,
                @RequestParam MultipartFile file
        ){
            ChunkUploadResponse response = uploadService.uploadChunk(videoId, chunkIndex, file);
            return ResponseEntity.ok(response);
        }

        // checked exceptions propagate up the call stack
        @PostMapping("/{videoId}/complete")
        ResponseEntity<CompleteUploadResponse> completeUpload(@PathVariable String videoId) throws Exception{
            CompleteUploadResponse response = uploadService.completeUpload(videoId);
            return ResponseEntity.ok(response);
        }



}
