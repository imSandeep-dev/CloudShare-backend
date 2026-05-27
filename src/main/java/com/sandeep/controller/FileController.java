package com.sandeep.controller;

import com.sandeep.document.UserCredits;
import com.sandeep.dto.FileMetadataDTO;
import com.sandeep.service.FileMetadataService;
import com.sandeep.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileMetadataService fileMetadataService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("files")MultipartFile[] multipartFile) throws IOException {
        Map<String,Object> response=new HashMap<>();
        List<FileMetadataDTO>list=fileMetadataService.uploadFiles(multipartFile);
        response.put("files",list);
        UserCredits finalCredits=userCreditsService.getUserCredits();
        response.put("remaining credits",finalCredits.getCredits());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getFiles(){
        List<FileMetadataDTO>files=fileMetadataService.getFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
        FileMetadataDTO file=fileMetadataService.getPublicFile(id);
        if(file == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","File is not public"));
        }
        return ResponseEntity.ok(file);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getViewUrl(@PathVariable String id){
        String viewUrl = fileMetadataService.getViewUrl(id);
        return ResponseEntity.ok(Map.of("url",viewUrl));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable String id) throws IOException {
        String downloadUrl = fileMetadataService.getDownloadUrl(id);
        return ResponseEntity.ok(Map.of("url",downloadUrl));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) throws IOException {
        fileMetadataService.deleteFile(id);
        return ResponseEntity.ok(Map.of("message","file deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id){
        FileMetadataDTO response=fileMetadataService.togglePublic(id);
        return ResponseEntity.ok(response);
    }
}
