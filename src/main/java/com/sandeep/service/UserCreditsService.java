package com.sandeep.service;

import com.sandeep.document.ProfileDocument;
import com.sandeep.document.UserCredits;
import com.sandeep.repository.ProfileRepository;
import com.sandeep.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserCreditsService {

    private final UserCreditsRepository userCreditsRepository;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;

    public UserCredits createInitialCredits(String clerkId){
        UserCredits userCredits=UserCredits
                .builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();
        return userCreditsRepository.save(userCredits);
    }

    public UserCredits getUserCredits(String clerkId){
        return userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(()->createInitialCredits(clerkId));
    }

    public UserCredits getUserCredits(){
        String clerkId=profileService.getCurrentProfile().getClerkId();
        return getUserCredits(clerkId);
    }

    public Boolean hasEnoughCredits(int requiredCredits){
        UserCredits userCredits=getUserCredits();
        return userCredits.getCredits()>=requiredCredits;
    }

    public UserCredits consumeCredit(){
        UserCredits userCredits=getUserCredits();
        if (userCredits.getCredits() <= 0) {
            return null;
        }
        userCredits.setCredits(userCredits.getCredits()-1);
        ProfileDocument currentProfile=profileService.getCurrentProfile();
        currentProfile.setCredits(userCredits.getCredits());
        profileRepository.save(currentProfile);
        return userCreditsRepository.save(userCredits);
    }

    public UserCredits addCredits(String clerkId,Integer creditsToAdd,String plan){
        UserCredits userCredits=userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(()->createInitialCredits(clerkId));
        userCredits.setCredits(userCredits.getCredits()+creditsToAdd);
        userCredits.setPlan(plan);
        return userCreditsRepository.save(userCredits);
    }

}
