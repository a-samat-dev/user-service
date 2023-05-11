package kz.smarthealth.userservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3control.model.AWSS3ControlException;
import kz.smarthealth.userservice.config.AmazonConfig;
import kz.smarthealth.userservice.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AmazonS3Service}
 *
 * Created by Samat Abibulla on 2023-05-08
 */
@ExtendWith(MockitoExtension.class)
class AmazonS3ServiceTest {

    @Captor
    private ArgumentCaptor<String> bucketNameCaptor;

    @Captor
    private ArgumentCaptor<String> fileNameCaptor;

    @Captor
    private ArgumentCaptor<InputStream> inputStreamCaptor;

    @Captor
    private ArgumentCaptor<ObjectMetadata> objectMetadataCaptor;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private AmazonConfig amazonConfig;

    @InjectMocks
    private AmazonS3Service underTest;

    @Test
    void uploadUserProfilePicture_uploadsSuccessfully() throws IOException {
        // given
        String bucketName = "bucket-name";
        String profilePicturesFolder = "profile-pictures-folder/";
        String fileName = UUID.randomUUID() + ".jpeg";
        MockMultipartFile file = new MockMultipartFile("file", "file.jpeg",
                "image/jpeg", "some xml".getBytes());
        when(amazonConfig.getS3BucketName()).thenReturn(bucketName);
        when(amazonConfig.getS3ProfilePicturesFolder()).thenReturn(profilePicturesFolder);
        // when
        underTest.uploadUserProfilePicture(fileName, file);
        // then
        verify(amazonS3).putObject(bucketNameCaptor.capture(), fileNameCaptor.capture(),
                inputStreamCaptor.capture(), objectMetadataCaptor.capture());

        String actualBucketName = bucketNameCaptor.getValue();
        String actualFileName = fileNameCaptor.getValue();
        InputStream actualInputStream = inputStreamCaptor.getValue();
        ObjectMetadata actualObjectMetadata = objectMetadataCaptor.getValue();

        assertEquals(bucketName, actualBucketName);
        assertEquals(profilePicturesFolder + fileName, actualFileName);
        assertNotNull(actualInputStream);
        assertEquals(file.getSize(), actualObjectMetadata.getContentLength());
    }

    @Test
    void uploadUserProfilePicture_throwsException() {
        // given
        String fileName = UUID.randomUUID() + ".jpeg";
        MockMultipartFile file = new MockMultipartFile("file", "file.jpeg",
                "image/jpeg", "some xml".getBytes());
        String bucketName = "bucket-name";
        String profilePicturesFolder = "profile-pictures-folder/";
        String customExceptionMessage = "Custom exception message";
        when(amazonConfig.getS3BucketName()).thenReturn(bucketName);
        when(amazonConfig.getS3ProfilePicturesFolder()).thenReturn(profilePicturesFolder);
        when(amazonS3.putObject(any(), any(), any(), any())).thenThrow(new AWSS3ControlException(customExceptionMessage));
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.uploadUserProfilePicture(fileName, file));
        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), exception.getError());
        assertTrue(exception.getErrorMessage().contains(customExceptionMessage));
    }


    @Test
    void generateProfilePicturePreSignedUrl_returnsEmptyString_whenProfilePictureDoesNotExist() {
        // given
        String fileName = UUID.randomUUID() + ".jpeg";
        String bucketName = "bucket-name";
        String profilePicturesFolder = "profile-pictures-folder/";
        when(amazonConfig.getS3BucketName()).thenReturn(bucketName);
        when(amazonConfig.getS3ProfilePicturesFolder()).thenReturn(profilePicturesFolder);
        when(amazonS3.doesObjectExist(bucketName, profilePicturesFolder + fileName)).thenReturn(false);
        // when
        String preSignedUrl = underTest.generateProfilePicturePreSignedUrl(fileName);
        // then
        assertTrue(preSignedUrl.isEmpty());
    }

    @Test
    void generateProfilePicturePreSignedUrl_returnsPreSignedUrl() throws MalformedURLException {
        // given
        String fileName = UUID.randomUUID() + ".jpeg";
        String bucketName = "bucket-name";
        String profilePicturesFolder = "profile-pictures-folder/";
        String expectedUrl = "http://expected-url.com";
        when(amazonConfig.getS3BucketName()).thenReturn(bucketName);
        when(amazonConfig.getS3ProfilePicturesFolder()).thenReturn(profilePicturesFolder);
        when(amazonS3.doesObjectExist(any(), any())).thenReturn(true);
        when(amazonS3.generatePresignedUrl(any(), any(), any(), any())).thenReturn(new URL(expectedUrl));
        // when
        String preSignedUrl = underTest.generateProfilePicturePreSignedUrl(fileName);
        // then
        assertNotNull(preSignedUrl);
        assertEquals(expectedUrl, preSignedUrl);
    }
}