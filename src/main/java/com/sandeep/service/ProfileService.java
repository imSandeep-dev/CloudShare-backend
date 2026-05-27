package com.sandeep.service;

import com.mongodb.MongoWriteException;
import com.sandeep.document.ProfileDocument;
import com.sandeep.dto.ProfileDto;
import com.sandeep.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileDto createProfile(ProfileDto profileDto){

        if(profileRepository.existsByClerkId(profileDto.getClerkId())){
            return updateProfile(profileDto);
        }

        ProfileDocument profile=ProfileDocument.builder()
                .clerkId(profileDto.getClerkId())
                .email(profileDto.getEmail())
                .firstName(profileDto.getFirstName())
                .lastName(profileDto.getLastName())
                .photoUrl(profileDto.getPhotoUrl())
                .credits(5)
                .createdAt(Instant.now())
                .build();

        profileRepository.save(profile);

        return ProfileDto.builder()
                .id(profile.getId())
                .clerkId(profile.getClerkId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .credits(profile.getCredits())
                .createdAt(profile.getCreatedAt())
                .build();

    }

    public ProfileDto updateProfile(ProfileDto profileDto){
        ProfileDocument existingProfile=profileRepository.findByClerkId(profileDto.getClerkId());
        if(existingProfile!=null){
            if(existingProfile.getEmail()!=null && !profileDto.getEmail().isEmpty()){
                existingProfile.setEmail(profileDto.getEmail());
            }
            if(existingProfile.getFirstName()!=null && !profileDto.getFirstName().isEmpty()){
                existingProfile.setFirstName(profileDto.getFirstName());
            }
            if(existingProfile.getLastName()!=null && !profileDto.getLastName().isEmpty()){
                existingProfile.setLastName(profileDto.getLastName());
            }
            if(existingProfile.getPhotoUrl()!=null && !profileDto.getPhotoUrl().isEmpty()){
                existingProfile.setPhotoUrl(profileDto.getPhotoUrl());
            }
            profileRepository.save(existingProfile);
            return ProfileDto.builder()
                    .id(existingProfile.getId())
                    .clerkId(existingProfile.getClerkId())
                    .email(existingProfile.getEmail())
                    .firstName(existingProfile.getFirstName())
                    .lastName(existingProfile.getLastName())
                    .photoUrl(existingProfile.getPhotoUrl())
                    .credits(existingProfile.getCredits())
                    .createdAt(existingProfile.getCreatedAt())
                    .build();
        }
        return null;
    }

    public boolean existsByClerkId(String clerkId) {
        return profileRepository.existsByClerkId(clerkId);
    }

    public void delete(String clerkId){
        ProfileDocument existingProfile=profileRepository.findByClerkId(clerkId);
        if(existingProfile!=null){
            profileRepository.delete(existingProfile);
//            return "Deleted successfully";
        }
//        return "User not found";
    }

    public ProfileDocument getCurrentProfile(){
        if(SecurityContextHolder.getContext().getAuthentication()==null){
            throw new RuntimeException("User not Authenticated");
        }
        String clerkId=SecurityContextHolder.getContext().getAuthentication().getName();
        return profileRepository.findByClerkId(clerkId);
    }

}
