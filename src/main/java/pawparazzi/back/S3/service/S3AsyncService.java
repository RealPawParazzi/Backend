package pawparazzi.back.S3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Service
public class S3AsyncService {
    private final S3AsyncClient s3AsyncClient;

    @Value("${aws.s3-bucket}")
    private String bucketName;

    public S3AsyncService(S3AsyncClient s3AsyncClient) {
        this.s3AsyncClient = s3AsyncClient;
    }

    /**
     * S3에 비동기적으로 파일 업로드
     */
    @Async
    public CompletableFuture<String> uploadFile(String fileName, byte[] fileData, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        return s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromByteBuffer(ByteBuffer.wrap(fileData)))
                .thenApply(response -> {
                    if (response.sdkHttpResponse().isSuccessful()) {
                        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
                    } else {
                        throw new RuntimeException("S3 업로드 실패: " + response.sdkHttpResponse().statusCode());
                    }
                });
    }

    /**
     * S3에서 비동기적으로 파일 삭제
     */
    @Async
    public CompletableFuture<Void> deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3AsyncClient.deleteObject(deleteObjectRequest)
                .thenAccept(response -> {
                    if (!response.sdkHttpResponse().isSuccessful()) {
                        throw new RuntimeException("S3 삭제 실패: " + response.sdkHttpResponse().statusCode());
                    }
                });
    }

    /**
     * 기존 프로필 이미지 삭제 후 새 이미지 업로드
     */
    public CompletableFuture<String> updateProfileImage(String existingImageUrl, String newFileName, byte[] newFileData, String contentType) {
        if (existingImageUrl != null && !existingImageUrl.isBlank()) {
            String oldFileName = extractFileName(existingImageUrl);
            deleteFile(oldFileName).join();
        }
        return uploadFile(newFileName, newFileData, contentType);
    }

    /**
     * S3 URL에서 파일 이름을 추출하는 메서드
     */
    public String extractFileName(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}