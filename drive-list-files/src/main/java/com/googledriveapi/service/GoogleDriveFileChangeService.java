package com.googledriveapi.service;

import com.elasticsearch.models.File;
import com.elasticsearch.repositories.FileRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GoogleDriveFileChangeService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private GoogleDriveFileListService googleDriveFileListService;

    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
            "https://www.googleapis.com/auth/drive.install");

    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    private static final String SAVED_NEXT_PAGE_TOKEN = "SAVED_NEXT_PAGE_TOKEN";

    @Value("${google.secret.key.path}")
    private Resource gdSecretKeys;

    @Value("${google.credentials.folder.path}")
    private Resource credentialsFolder;

    private GoogleAuthorizationCodeFlow flow;
    private Drive drive;

    @PostConstruct
    public void init() throws Exception {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(gdSecretKeys.getInputStream()));
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();

        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringbootexample").build();
    }


    @Async("threadPoolTaskExecutor")
    public void getChangedFiles() throws IOException {
        String savedStartPageToken = null;

        if(savedStartPageToken == null){
            savedStartPageToken = drive.changes().getStartPageToken().execute().getStartPageToken();
        }
        String pageToken = savedStartPageToken;
        while (pageToken != null) {
            ChangeList changes = drive.changes().list(pageToken).setIncludeRemoved(true).setRestrictToMyDrive(true).setIncludeCorpusRemovals(true).execute();
            for (Change change : changes.getChanges()) {
                // Process change
                log.info("file change detected File Id: {}, remove: {}", change.getFileId(), change.getRemoved());
                String fileId = change.getFileId();

                boolean isRemoved = change.getRemoved();
                if(isRemoved){
                    log.info("file removed from google drive, removing from elastic search...");
                    Optional<File> getFileFromElasticSearch = fileRepository.findById(fileId);
                    if(getFileFromElasticSearch.isPresent())fileRepository.delete(getFileFromElasticSearch.get());
                    log.info("file removed from elastic search ...DONE");
                }else if(!fileRepository.findById(fileId).isPresent() && !isRemoved){
                    log.error("retriving file fileId: {} from google drive...", fileId);
                    File file = googleDriveFileListService.retriveFileFromGoogleDriveWithId(fileId);
                    log.info("new file added in google drive, loading to elastic search...");
                    fileRepository.save(file);
                    log.info("loding to elastic search ...DONE");
                }
            }
            if (changes.getNewStartPageToken() != null) {
                // Last page, save this token for the next polling interval
                savedStartPageToken = changes.getNewStartPageToken();
            }
            pageToken = changes.getNewStartPageToken();
        }
    }
}
