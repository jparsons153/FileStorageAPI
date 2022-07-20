package com.FileStorage.API.controllers;

import com.FileStorage.API.models.DatabaseFile;
import com.FileStorage.API.models.FileResponse;
import com.FileStorage.API.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/uploadSingleFile")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        try {
            return ResponseEntity.ok(fileService.saveFile(file));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFileById(@PathVariable(name = "id") Long fileId) {

        DatabaseFile databaseFile = fileService.getFile(fileId);

        if (databaseFile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found with id: " + fileId);
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(databaseFile.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", databaseFile.getFileName()))
                    .body(new ByteArrayResource(databaseFile.getData()));
        }
    }

    @PutMapping("/uploadSingleFile/{id}")
    public ResponseEntity<?> updateFileById(@PathVariable(name = "id") Long fileId, @RequestBody MultipartFile file) {

        if (fileService.fileDoesNotExist(fileId)) {
            return ResponseEntity.badRequest()
                    .body(new NoSuchFileException("The ID you passed in was not valid. Where you trying to upload a new file?"));
        } else {
            try {
                return ResponseEntity.ok(fileService.updateFile(fileId, file));
            } catch (Exception ex) {
                return ResponseEntity.badRequest()
                        .body(ex.getMessage());
            }
        }
    }

    @DeleteMapping("/deleteFile/{id}")
    public ResponseEntity<?> deleteFileById(@PathVariable("id") Long fileId) {
        if (fileService.fileDoesNotExist(fileId)) {
            return ResponseEntity.badRequest()
                    .body(new NoSuchFileException("The ID you passed in was not valid."));
        } else {
            fileService.deleteFile(fileId);
            return ResponseEntity.ok("File with ID " + fileId + " was deleted.");
        }
    }

    //    @GetMapping("/files")
//    public List<DatabaseFile> fileList(){
//        List<DatabaseFile> databaseFile = fileService.getAllFiles();
//    return databaseFile;
//    }

    @GetMapping("/files/ResponseEntity")
    public ResponseEntity<?> fileResponseEntityMap(){

        List<DatabaseFile> databaseFile = fileService.getAllFiles();

        for (DatabaseFile file:databaseFile){
            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found with id: " + file.getId());
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(file.getFileType()))
//                      .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getFileName()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(new ByteArrayResource(file.getData()));
            }
        }
        return null;
    }

    // add duplicateFile() method
    // save file to database without passing in Multipart file?
    @PostMapping("/duplicateFile/{id}/{name}")
    public ResponseEntity<?> duplicateFile(@PathVariable(name = "id") Long fileId, @PathVariable(name = "name") String duplicateName) throws IOException {
    // @RequestBody MultipartFile file) {
        // get file to copy
        final Optional<DatabaseFile> optional = Optional.ofNullable(fileService.getFile(fileId));

        // check if file ID is valid
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new NoSuchFileException("The ID you passed in was not valid. " +
                            "Where you trying to upload a new file?"));
        } else if (optional == null) {
            return ResponseEntity.badRequest()
                    .body(new NoSuchFileException("No file was received, please try again."));
        }

        // set databaseFile to file from get request
        DatabaseFile databaseFile = optional.get();
        DatabaseFile duplicateFile = databaseFile;

        // set data, fileName & fileType
        duplicateFile.setData(optional.get().getData());
        duplicateFile.setFileName(duplicateName);
        duplicateFile.setFileType(databaseFile.getFileType());

       // final DatabaseFile savedFile = fileService.updateFile(duplicateFile.getId(),file);

        duplicateFile.setDownloadUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(String.valueOf(duplicateFile.getId()))
                .toUriString());

        return ResponseEntity.ok(FileResponse.builder()
                .fileName(duplicateFile.getFileName())
                .fileDownloadUri(duplicateFile.getDownloadUrl())
                .fileType(duplicateFile.getFileType())
                .size(duplicateFile.getSize())
                .build());
    }
}