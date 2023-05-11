package kz.smarthealth.userservice.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import kz.smarthealth.userservice.config.AmazonConfig;
import kz.smarthealth.userservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;
import java.util.Date;

/**
 * Class that works with AWS S3 service.
 *
 * Created by Samat Abibulla on 2023-05-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3Service {

    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;

    /**
     * Uploads user's profile picture
     *
     * @param fileName profile picture file name
     * @param file     actual image file
     */
    public void uploadUserProfilePicture(String fileName, MultipartFile file) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());

            amazonS3.putObject(amazonConfig.getS3BucketName(),
                    amazonConfig.getS3ProfilePicturesFolder() + fileName, file.getInputStream(),
                    objectMetadata);
        } catch (Exception e) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Generates pre-signed url with the access to user's profile picture
     *
     * @param fileName user profile pictures file name
     * @return pre-signed url
     */
    public String generateProfilePicturePreSignedUrl(String fileName) {
        if (!amazonS3.doesObjectExist(amazonConfig.getS3BucketName(),
                amazonConfig.getS3ProfilePicturesFolder() + fileName)) {
            return StringUtils.EMPTY;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);

        return amazonS3.generatePresignedUrl(amazonConfig.getS3BucketName(), amazonConfig.getS3ProfilePicturesFolder()
                + fileName, calendar.getTime(), HttpMethod.GET).toString();
    }
}
