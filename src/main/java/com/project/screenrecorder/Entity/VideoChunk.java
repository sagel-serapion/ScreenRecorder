package com.project.screenrecorder.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_chunk")
@Data
public class VideoChunk {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long videoId;

    private int chunkIndex;

    private String minioPath;

    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate(){
        uploadedAt = LocalDateTime.now();
    }



}
