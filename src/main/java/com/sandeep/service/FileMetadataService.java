package com.sandeep.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.sandeep.document.FileMetadataDocument;
import com.sandeep.document.ProfileDocument;
import com.sandeep.document.UserCredits;
import com.sandeep.dto.FileMetadataDTO;
import com.sandeep.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileRepository;
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final Cloudinary cloudinary;

    public List<FileMetadataDTO> uploadFiles(MultipartFile[] files) throws IOException {
        ProfileDocument currentProfile=profileService.getCurrentProfile();
        List<FileMetadataDocument> savedFiles=new ArrayList<>();
        if(!userCreditsService.hasEnoughCredits(files.length)){
            throw new RuntimeException("Not enough credits");
        }
        for(MultipartFile file:files){
            File tempFile = File.createTempFile("upload_","_"+file.getOriginalFilename());
            file.transferTo(tempFile);
            try {
                Map uploadResult = cloudinary.uploader().upload(tempFile,ObjectUtils.asMap(
                        "resource_type","auto",
                        "folder","cloudshare/"+currentProfile.getClerkId(),
                        "use_filename",true,
                        "unique_filename",true
                ));
                String publicId = (String) uploadResult.get("public_id");
                String secureUrl = (String) uploadResult.get("secure_url");
                long bytes = ((Number) uploadResult.get("bytes")).longValue();
                FileMetadataDocument fileMetadataDocument = FileMetadataDocument.builder()
                        .name(file.getOriginalFilename())
                        .type(file.getContentType())
                        .size(bytes)
                        .clerkId(currentProfile.getClerkId())
                        .isPublic(false)
                        .fileLocation(secureUrl)
                        .cloudinaryPublicId(publicId)
                        .uploadedAt(LocalDateTime.now())
                        .build();
                userCreditsService.consumeCredit();
                savedFiles.add(fileMetadataDocument);
            }finally {
                tempFile.delete();
            }
        }
        fileRepository.saveAll(savedFiles);
        return savedFiles.stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public String getViewUrl(String id) {
        FileMetadataDocument file = findAndAuthorize(id);
        return file.getFileLocation();
    }

    private FileMetadataDocument findAndAuthorize(String id) {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        FileMetadataDocument file = fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("File not found")
        );
        if(!file.getClerkId().equals(currentProfile.getClerkId()) && !file.getIsPublic()){
            throw new RuntimeException("Access denied");
        }
        return file;
    }

    private FileMetadataDTO mapToDto(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .fileLocation(fileMetadataDocument.getFileLocation())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.getIsPublic())
                .uploadedAt(fileMetadataDocument.getUploadedAt())
                .build();
    }

    public List<FileMetadataDTO> getFiles(){
        ProfileDocument currentProfile=profileService.getCurrentProfile();
        List<FileMetadataDocument> files=fileRepository.findByClerkId(currentProfile.getClerkId());
        return files.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public FileMetadataDTO getPublicFile(String id){
        Optional<FileMetadataDocument> file=fileRepository.findById(id);
        if(file.isEmpty() || !file.get().getIsPublic()){
            return null;
        }
        FileMetadataDocument fileMetadataDocument=file.get();
        return mapToDto(fileMetadataDocument);
    }

    public FileMetadataDTO downloadableFile(String id){
        FileMetadataDocument file=fileRepository.findById(id).orElseThrow(()->new RuntimeException("File not found"));
        return mapToDto(file);
    }

    public void deleteFile(String id){
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        FileMetadataDocument file = fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("File not found")
        ) ;
        if(!file.getClerkId().equals(currentProfile.getClerkId())){
            throw new RuntimeException("File does not belong to current user");
        }
        try {
            if(file.getCloudinaryPublicId()!=null){
                cloudinary.uploader().destroy(file.getCloudinaryPublicId(),ObjectUtils.asMap(
                        "resource_type",resolveResourceType(file.getType())
                ));
                fileRepository.deleteById(id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting the file");
        }
    }

    private String resolveResourceType(String mimeType) {
        if(mimeType == null)return "raw";
        if(mimeType.startsWith("image/"))return "image";
        if (mimeType.startsWith("video/"))return "video";
        return "raw";
    }

    public String getDownloadUrl(String id) {
        FileMetadataDocument file = findAndAuthorize(id);
        String safeFilename = file.getName().replace(" ", "_");

        // Remove extension from the flag — Cloudinary adds it automatically
        String filenameWithoutExt = safeFilename.contains(".")
                ? safeFilename.substring(0, safeFilename.lastIndexOf("."))
                : safeFilename;

        return file.getFileLocation()
                .replace("/upload/", "/upload/fl_attachment:" + filenameWithoutExt + "/");
    }

    private String extractResourceTypeFromUrl(String url) {
        if (url.contains("/image/upload/")) return "image";
        if (url.contains("/video/upload/")) return "video";
        return "raw";
    }

    public FileMetadataDTO togglePublic(String id){
        FileMetadataDocument file=fileRepository.findById(id)
                .orElseThrow(()->new RuntimeException("File is not found"));
        file.setIsPublic(!file.getIsPublic());
        fileRepository.save(file);
        return mapToDto(file);
    }

}
