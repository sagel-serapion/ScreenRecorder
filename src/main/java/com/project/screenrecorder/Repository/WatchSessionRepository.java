package com.project.screenrecorder.Repository;


import com.project.screenrecorder.Entity.WatchSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchSessionRepository extends JpaRepository<WatchSession,String> {

     List<WatchSession> findByVideoId(String videoId);

     Optional<WatchSession> findTopByVideoIdAndViewerIpOrderByStartedAtDesc(String videoId,String viewerIp);


}
