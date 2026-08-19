package com.securevault.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project.id:secure-vault}")
    private String projectId;

    private boolean initialized = false;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            initialized = true;
            return;
        }

        try {
            FirebaseOptions options = null;

            // Priority 1: explicit credentials file path
            if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                File credentialsFile = new File(credentialsPath.trim());
                if (credentialsFile.exists()) {
                    try (InputStream serviceAccount = new FileInputStream(credentialsFile)) {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .setProjectId(projectId)
                                .build();
                        log.info("Initialized Firebase from service account file: {}", credentialsPath);
                    }
                } else {
                    log.warn("Firebase credentials file not found at: {}. Attempting application default.", credentialsPath);
                }
            }

            // Priority 2: Google Application Default Credentials (gcloud auth, env var, etc.)
            if (options == null) {
                try {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(projectId)
                            .build();
                    log.info("Initialized Firebase using Google Application Default credentials.");
                } catch (Exception e) {
                    // No credentials at all — skip Firebase initialization entirely.
                    // In dev mode the FirebaseAuthenticationFilter passes requests through
                    // when FirebaseAuth is null, using the bearer token as a passthrough identity.
                    log.warn("No Firebase credentials available. Running in dev/offline mode. " +
                             "Set firebase.credentials.path or GOOGLE_APPLICATION_CREDENTIALS to enable token verification.");
                    return;
                }
            }

            FirebaseApp.initializeApp(options);
            initialized = true;
            log.info("Firebase Application initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (!initialized && FirebaseApp.getApps().isEmpty()) {
            initializeFirebase();
        }
        try {
            return FirebaseAuth.getInstance();
        } catch (Exception e) {
            log.warn("FirebaseAuth instance could not be retrieved: {}", e.getMessage());
            return null;
        }
    }
}
