package pawparazzi.back.S3;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.service.S3AsyncService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class S3UploadUtil {

    private final S3AsyncService s3AsyncService;

    public S3UploadUtil(S3AsyncService s3AsyncService) {
        this.s3AsyncService = s3AsyncService;
    }

    public CompletableFuture<String> uploadImageAsync(MultipartFile file, String pathPrefix, String defaultImageUrl) {
        if (file == null || file.isEmpty()) {
            return CompletableFuture.completedFuture(defaultImageUrl);
        }

        try {
            String fileName = pathPrefix + "_" + System.currentTimeMillis();
            return s3AsyncService.uploadFile(fileName, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("파일 업로드 실패: " + e.getMessage()));
            return failedFuture;
        }
    }
}