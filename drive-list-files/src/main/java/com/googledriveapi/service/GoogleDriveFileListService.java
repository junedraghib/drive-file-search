package com.googledriveapi.service;

import com.adobeextractpdfapi.AdobeAPI;
import com.elasticsearch.models.File;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.google.api.services.drive.model.FileList;
import com.googledriveapi.model.Root;
import com.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleDriveFileListService {
    @Autowired
    GoogleDriveFileDownloadService googleDriveFileDownloadService;

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


    public List<File> retriveFilesFromGoogleDrive() throws IOException{
        List<File> responseList = new ArrayList<>();

        FileList fileList = drive.files().list().setFields("files(id,name,mimeType,webContentLink)").setQ("mimeType = 'application/pdf'").execute();
        for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
            File item = new File();

            item.setId(file.getId());
            item.setFileName(file.getName());
            item.setWebContentLink(file.getWebContentLink());

            Files.deleteIfExists(new java.io.File("json.zip").toPath());
            AdobeAPI.extractPDFContent(googleDriveFileDownloadService.downloadToInputStream(file.getId()), "json");
            Utils.unzip("json.zip", "json");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            Root result = objectMapper.readValue(new java.io.File("json//structuredData.json"), Root.class);

            java.util.List<Root.Element> elements =  result.getElements();
            StringBuilder content = new StringBuilder("");
            for(Root.Element element: elements){
                content.append(element.getText());
            }
            item.setFileContent(content.toString());
            responseList.add(item);
        }

        return responseList;
    }

    public File retriveFileFromGoogleDriveWithId(String id) throws IOException{
        com.google.api.services.drive.model.File file = drive.files().get(id).execute();
        File item = new File();

        item.setId(file.getId());
        item.setFileName(file.getName());
        item.setWebContentLink(file.getWebContentLink());

        Files.deleteIfExists(new java.io.File("json.zip").toPath());
        AdobeAPI.extractPDFContent(googleDriveFileDownloadService.downloadToInputStream(file.getId()), "json");
        Utils.unzip("json.zip", "json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Root result = objectMapper.readValue(new java.io.File("json//structuredData.json"), Root.class);

        java.util.List<Root.Element> elements =  result.getElements();
        StringBuilder content = new StringBuilder("");
        for(Root.Element element: elements){
            content.append(element.getText());
        }
        item.setFileContent(content.toString());
        return item;
    }
}

