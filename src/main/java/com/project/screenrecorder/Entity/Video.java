package com.project.screenrecorder.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "video")
@Data
public class Video {


        public enum VideoStatus{
                UPLOADING , PROCESSING , READY
        }

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        private String title;

        @Column(unique = true)
        private String token;

        private String passwordHash;

        private String minioPath;

        private String thumbnailPath;

        @Enumerated(EnumType.STRING)
        private VideoStatus status;

        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate(){
                createdAt= LocalDateTime.now();
        }








}
