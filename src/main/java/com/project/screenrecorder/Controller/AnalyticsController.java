package com.project.screenrecorder.Controller;


import com.project.screenrecorder.DTO.analytics.PingRequest;
import com.project.screenrecorder.Service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/watch")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/{token}/ping")
    public ResponseEntity<Void> getPing(
            @PathVariable String token,
            @RequestBody PingRequest pingRequest,
            HttpServletRequest request
            ){
        analyticsService.recordPing(token,request, pingRequest.getPosition());
        return ResponseEntity.ok().build();
    }

}
