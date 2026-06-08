package com.project.screenrecorder.Controller;



import com.project.screenrecorder.DTO.watch.VideoAuthRequest;
import com.project.screenrecorder.DTO.watch.WatchUrlResponse;
import com.project.screenrecorder.Security.SecurityBridge;
import com.project.screenrecorder.Service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/watch")
@RequiredArgsConstructor
public class WatchController {

    private final WatchService watchService;


    @GetMapping("/{token}")
    ResponseEntity<WatchUrlResponse> watchUrl(
            @PathVariable String token
    ){
        WatchUrlResponse responseUrl = watchService.getWatchUrl(token );
        return ResponseEntity.ok(responseUrl);
    }

    @PostMapping("/{token}/auth")
    ResponseEntity<Map<String,String>> authenticate(
            @PathVariable String token,
            @RequestBody  VideoAuthRequest videoAuthRequest
            ){
        return  watchService.authenticateVideo(token , videoAuthRequest.getPassword());
    }

    @GetMapping("/{token}/stream")
    ResponseEntity<WatchUrlResponse> watchUrlStream(
            @AuthenticationPrincipal SecurityBridge current ,
            @PathVariable String token
    ){
        WatchUrlResponse reponseUrl = watchService.getWatchUrl(token , current);
        return ResponseEntity.ok(reponseUrl);
    }


}
