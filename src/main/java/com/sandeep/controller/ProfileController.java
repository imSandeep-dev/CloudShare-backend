package com.sandeep.controller;

import com.sandeep.document.UserCredits;
import com.sandeep.dto.ProfileDto;
import com.sandeep.dto.UserCreditsDto;
import com.sandeep.service.ProfileService;
import com.sandeep.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/register")
    public ResponseEntity<?> createProfile(@RequestBody ProfileDto profileDto){

        HttpStatus status=profileService.existsByClerkId(profileDto.getClerkId()) ? HttpStatus.OK : HttpStatus.CREATED;

        ProfileDto profile=profileService.createProfile(profileDto);
        return ResponseEntity.status(status).body(profile);
    }

    @DeleteMapping("/delete/{clerkId}")
    public ResponseEntity<?> deleteProfile(@PathVariable String clerkId){
        profileService.delete(clerkId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","user deleted"));
    }

    @GetMapping("/users/credits")
    public ResponseEntity<?> getCredits(){
        UserCredits userCredits=userCreditsService.getUserCredits();
        UserCreditsDto response=UserCreditsDto.builder()
                .credits(userCredits.getCredits())
                .plan(userCredits.getPlan())
                .build();
        return ResponseEntity.ok(response);
    }

}
