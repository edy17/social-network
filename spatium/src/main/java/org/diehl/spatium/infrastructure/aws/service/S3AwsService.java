package org.diehl.spatium.infrastructure.aws.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class S3AwsService {

    private static Logger logger = LoggerFactory.getLogger(S3AwsService.class);

    @ConfigProperty(name = "quarkus.dynamodb.aws.credentials.static-provider.access-key-id")
    String accesskey;

    @ConfigProperty(name = "quarkus.dynamodb.aws.credentials.static-provider.secret-access-key")
    String secretKey;

    @ConfigProperty(name = "quarkus.dynamodb.aws.region")
    String region;

    @ConfigProperty(name = "spatium.image.bucket.name")
    String bucketName;

    private AmazonS3 s3client;

    @PostConstruct
    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretKey);
        this.s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    public void configBucket() {
        if (s3client.doesBucketExistV2(bucketName)) {
            logger.info("Bucket name is not available.");
            s3client.createBucket(bucketName);
            logger.info("Bucket {} was creates.", bucketName);
        }
    }

    public PutObjectResult addImage(String imageKey, InputStream inputStream) {
        try {
            File file = File.createTempFile(imageKey,".spatium");
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[inputStream.available()];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            return s3client.putObject(bucketName, imageKey, file);
        } catch (IOException e) {
            logger.error("An exception occurred when preparing image to s3 upload", e);
        }
        throw new IllegalStateException("uploading image to s3 failed!");
    }

    public InputStream getImage(String imageKey) {
        return s3client.getObject(bucketName, imageKey).getObjectContent().getDelegateStream();
    }

    public void deleteImage(String imageKey) {
        s3client.deleteObject(bucketName, imageKey);
    }
}
