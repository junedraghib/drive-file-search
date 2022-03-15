package com.elasticsearch.services;

import com.elasticsearch.models.File;
import com.elasticsearch.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FileSearchServiceWithRepository {
    private FileRepository fileRepository;

    @Autowired
    public FileSearchServiceWithRepository(final FileRepository fileRepository){
        super();
        this.fileRepository = fileRepository;
    }

    public void createFileIndexBulk(final List<File> files){
        fileRepository.saveAll(files);
    }

    public void createProductIndex(final File file){
        fileRepository.save(file);
    }

    public List<File> findByFileName(final String fileName){
        return fileRepository.findByFileName(fileName);
    }
}
