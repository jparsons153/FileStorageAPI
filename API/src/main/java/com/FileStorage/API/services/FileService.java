package com.FileStorage.API.services;

import com.FileStorage.API.models.DatabaseFile;
import com.FileStorage.API.models.FileResponse;
import com.FileStorage.API.repositories.DatabaseFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    @Autowired
    DatabaseFileRepository fileRepository;

    public Long getDatabaseSize(){
        long size = fileRepository.count();
        return size;
    }

//    public List<DatabaseFile> getAllFiles(){
//
//        List<DatabaseFile> databaseFileList = new ArrayList<>();
//
//        Iterator<DatabaseFile> databaseFileIterator = fileRepository.findAll().iterator();
//
//        while(databaseFileIterator.hasNext()) {
//            databaseFileList.toArray();
//        }
//
//        return databaseFileList;
//    }

    public List<DatabaseFile> getAllFiles(){
        return fileRepository.findAll();
    }

    public DatabaseFile getFile(Long id) {
        if (fileRepository.findById(id).isEmpty()) {
            return null;
        } else {
            return fileRepository.findById(id).get();
        }
    }

    public FileResponse saveFile(MultipartFile multipartFile) throws Exception {
        validateMultiPartFile(multipartFile);
        final DatabaseFile savedFile = fileRepository.save(constructDatabaseFileFromMultiPart(multipartFile));
        return buildFileResponseFromDatabaseFile(savedFile);
    }

    public FileResponse updateFile(Long fileId, MultipartFile multipartFile) throws IOException {
        validateMultiPartFile(multipartFile);
        DatabaseFile databaseFile = getFile(fileId);
        databaseFile.setData(multipartFile.getBytes());
        databaseFile.setFileName(multipartFile.getOriginalFilename());
        databaseFile.setFileType(multipartFile.getContentType());
        final DatabaseFile savedFile = fileRepository.save(databaseFile);
        return buildFileResponseFromDatabaseFile(savedFile);
    }

    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    public boolean fileDoesNotExist(Long fileId) {
        return !fileRepository.existsById(fileId);
    }

    private void validateMultiPartFile(MultipartFile multipartFile) throws IllegalStateException {
        if (multipartFile == null) {
            throw new IllegalStateException("No file received, try again?");
        } else if (multipartFile.getOriginalFilename() == null) {
            throw new IllegalStateException("You must specify a file name!");
        } else if (multipartFile.isEmpty()) {
            throw new IllegalStateException("No file present!");
        }
    }

    private DatabaseFile constructDatabaseFileFromMultiPart(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        return DatabaseFile.builder()
                .data(file.getBytes())
                .fileName(fileName)
                .fileType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    private FileResponse buildFileResponseFromDatabaseFile(DatabaseFile databaseFile) {
        return FileResponse.builder()
                .fileName(databaseFile.getFileName())
                .fileDownloadUri(databaseFile.getDownloadUrl())
                .fileType(databaseFile.getFileType())
                .size(databaseFile.getSize())
                .build();
    }
}
