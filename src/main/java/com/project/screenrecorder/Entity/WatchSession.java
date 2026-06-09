package com.project.screenrecorder.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name= "watch_session")
@Data
public class WatchSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String videoId;

    private String viewerIp;

    private LocalDateTime startedAt;

    private LocalDateTime lastseenAt;

    private int lastPosition;

    private  int  watchDuration;

    @PrePersist
    protected void onCreate(){
         startedAt = LocalDateTime.now();
    }

}
