package com.project.screenrecorder.Repository;


import com.project.screenrecorder.Entity.WatchSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchSessionRepository extends JpaRepository<WatchSession,String> {





}
