package kz.smarthealth.userservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AmazonConfig {

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.s3.region}")
    private String s3Region;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${aws.s3.profile-pictures-folder}")
    private String s3ProfilePicturesFolder;
}
