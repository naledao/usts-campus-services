package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.update.android")
public class AndroidAppUpdateProperties {

    private boolean enabled = true;
    private String endpoint;
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;
    private String bucket = "usts-campus-services";
    private String metadataObject = "android/latest.json";
    private int presignedUrlExpirySeconds = 3600;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getMetadataObject() {
        return metadataObject;
    }

    public void setMetadataObject(String metadataObject) {
        this.metadataObject = metadataObject;
    }

    public int getPresignedUrlExpirySeconds() {
        return presignedUrlExpirySeconds;
    }

    public void setPresignedUrlExpirySeconds(int presignedUrlExpirySeconds) {
        this.presignedUrlExpirySeconds = presignedUrlExpirySeconds;
    }
}
