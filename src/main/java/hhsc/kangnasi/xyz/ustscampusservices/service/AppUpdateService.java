package hhsc.kangnasi.xyz.ustscampusservices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhsc.kangnasi.xyz.ustscampusservices.config.AndroidAppUpdateProperties;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.AndroidAppUpdateMetadata;
import hhsc.kangnasi.xyz.ustscampusservices.domain.vo.AppUpdateInfo;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;

@Service
@RegisterReflectionForBinding(AndroidAppUpdateMetadata.class)
public class AppUpdateService {

    private final AndroidAppUpdateProperties properties;
    private final ObjectMapper objectMapper;
    private volatile MinioClient minioClient;

    public AppUpdateService(AndroidAppUpdateProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public AppUpdateInfo getAndroidLatest(int currentVersionCode) {
        AppUpdateInfo info = new AppUpdateInfo();
        if (!properties.isEnabled()) {
            info.setUpdateAvailable(false);
            info.setLatestVersionCode(currentVersionCode);
            return info;
        }

        AndroidAppUpdateMetadata metadata = loadMetadata();
        validateMetadata(metadata);

        boolean updateAvailable = metadata.getVersionCode() > currentVersionCode;
        info.setUpdateAvailable(updateAvailable);
        info.setLatestVersionCode(metadata.getVersionCode());
        info.setLatestVersionName(metadata.getVersionName());
        info.setReleaseNotes(metadata.getReleaseNotes());
        info.setForceUpdate(metadata.isForceUpdate());
        info.setSha256(metadata.getSha256());
        info.setFileName(resolveFileName(metadata));

        if (updateAvailable) {
            info.setApkSize(resolveApkSize(metadata.getApkObject()));
            info.setApkUrl(createDownloadUrl(metadata.getApkObject()));
        }
        return info;
    }

    private AndroidAppUpdateMetadata loadMetadata() {
        try (InputStream stream = client().getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(properties.getMetadataObject())
                        .build())) {
            return objectMapper.readValue(stream, AndroidAppUpdateMetadata.class);
        } catch (Exception e) {
            throw new IllegalStateException("读取安卓更新元数据失败: " + e.getMessage(), e);
        }
    }

    private void validateMetadata(AndroidAppUpdateMetadata metadata) {
        if (metadata == null) {
            throw new IllegalStateException("安卓更新元数据为空");
        }
        if (metadata.getVersionCode() <= 0) {
            throw new IllegalStateException("安卓更新元数据 versionCode 无效");
        }
        if (!StringUtils.hasText(metadata.getVersionName())) {
            throw new IllegalStateException("安卓更新元数据 versionName 不能为空");
        }
        if (!StringUtils.hasText(metadata.getApkObject())) {
            throw new IllegalStateException("安卓更新元数据 apkObject 不能为空");
        }
    }

    private Long resolveApkSize(String apkObject) {
        try {
            StatObjectResponse stat = client().statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(apkObject)
                            .build());
            return stat.size();
        } catch (Exception e) {
            throw new IllegalStateException("读取 APK 文件信息失败: " + e.getMessage(), e);
        }
    }

    private String createDownloadUrl(String apkObject) {
        try {
            return client().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(apkObject)
                            .expiry(properties.getPresignedUrlExpirySeconds())
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("生成 APK 下载地址失败: " + e.getMessage(), e);
        }
    }

    private String resolveFileName(AndroidAppUpdateMetadata metadata) {
        if (StringUtils.hasText(metadata.getFileName())) {
            return metadata.getFileName();
        }
        return "usts-campus-services-" + metadata.getVersionName() + ".apk";
    }

    private MinioClient client() {
        MinioClient current = minioClient;
        if (current != null) {
            return current;
        }
        validateProperties();
        synchronized (this) {
            if (minioClient == null) {
                minioClient = MinioClient.builder()
                        .endpoint(properties.getEndpoint())
                        .region(properties.getRegion())
                        .credentials(properties.getAccessKey(), properties.getSecretKey())
                        .build();
            }
            return minioClient;
        }
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalStateException("MinIO endpoint 未配置");
        }
        if (!StringUtils.hasText(properties.getRegion())) {
            throw new IllegalStateException("MinIO region 未配置");
        }
        if (!StringUtils.hasText(properties.getAccessKey())) {
            throw new IllegalStateException("MinIO accessKey 未配置");
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalStateException("MinIO secretKey 未配置");
        }
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalStateException("MinIO bucket 未配置");
        }
        if (!StringUtils.hasText(properties.getMetadataObject())) {
            throw new IllegalStateException("MinIO metadataObject 未配置");
        }
    }
}
