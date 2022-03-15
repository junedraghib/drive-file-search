package com.googledriveapi.controller;

import com.elasticsearch.models.File;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.googledriveapi.service.GoogleDriveFileChangeService;
import com.googledriveapi.service.GoogleDriveFileListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Controller
public class GoogleDriveController {

    @Autowired
    GoogleDriveFileListService googleDriveFileListService;

    @Autowired
    GoogleDriveFileChangeService googleDriveFileChangeService;

    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
            "https://www.googleapis.com/auth/drive.install");

    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    private static final String SAVED_NEXT_PAGE_TOKEN = "SAVED_NEXT_PAGE_TOKEN";

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

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

    @GetMapping(value = { "/" })
    public String showHomePage() throws Exception {
        boolean isUserAuthenticated = false;

        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        if (credential != null) {
            boolean tokenValid = credential.refreshToken();
            if (tokenValid) {
                isUserAuthenticated = true;
            }
        }

        return isUserAuthenticated ? "dashboard.html" : "index.html";
    }

    @GetMapping(value = { "/googlesignin" })
    public void doGoogleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping(value = { "/oauth" })
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code);

            return "dashboard.html";
        }

        return "index.html";
    }

    @GetMapping(value = { "/files" }, produces = { "application/json" })
    public @ResponseBody
    List<File> listFiles() throws Exception {
        return googleDriveFileListService.retriveFilesFromGoogleDrive();
    }

    @GetMapping(value = { "/chnagefiles" }, produces = { "application/json" })
    public @ResponseBody void listChangeFiles(HttpSession httpSession) throws Exception {
        googleDriveFileChangeService.getChangedFiles();
    }

    private void saveToken(String code) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
    }
}
