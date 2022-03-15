package com;

import com.elasticsearch.models.File;
import com.elasticsearch.repositories.FileRepository;
import com.googledriveapi.service.GoogleDriveFileChangeService;
import com.googledriveapi.service.GoogleDriveFileListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Slf4j
public class DriveListFilesApplication {

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private GoogleDriveFileListService googleDriveFileListService;

	@Autowired
	private GoogleDriveFileChangeService googleDriveFileChangeService;


	public static void main(String[] args) {
		SpringApplication.run(DriveListFilesApplication.class, args);
	}

	@PreDestroy
	public void deleteIndex(){
		elasticsearchOperations.indexOps(File.class).delete();
	}

	@PostConstruct
	public void createIndex(){
		elasticsearchOperations.indexOps(File.class).refresh();
		fileRepository.deleteAll();
        List<File> initialFiles = new ArrayList<>();
		try{
		    log.info("retriving existing file from google drive...");
            initialFiles = googleDriveFileListService.retriveFilesFromGoogleDrive();
            log.info("existing file retrival ...DONE, retrieved {} files", initialFiles.size());
        }catch (IOException e){
		    log.error("error while retriving initial files {}", e.getMessage());
        }

		log.info("loading initial files into elastic search...");
        fileRepository.saveAll(initialFiles);
        log.info("loading of existing files in elasticsearch ...DONE");

        log.info("initiating google drive file change api, to look for changes in file...");
		try {
			googleDriveFileChangeService.getChangedFiles();
		} catch (IOException e) {
			log.error("error while looking for file changes {}", e.getMessage());
		}
		log.info("watching for file change on seperate thread...");
	}


}
