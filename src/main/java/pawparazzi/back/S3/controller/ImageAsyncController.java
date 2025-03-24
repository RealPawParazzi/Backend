package pawparazzi.back.S3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.service.S3AsyncService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async/images")
public class ImageAsyncController {

    private final S3AsyncService s3AsyncService;

    public ImageAsyncController(S3AsyncService s3AsyncService) {
        this.s3AsyncService = s3AsyncService;
    }

    /**
     * 비동기적으로 이미지 업로드
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            byte[] fileBytes = file.getBytes();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            return s3AsyncService.uploadFile(fileName, fileBytes, contentType)
                    .thenApply(url -> ResponseEntity.ok("File uploaded successfully: " + url));
        } catch (IOException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("File upload failed: " + e.getMessage()));
        }
    }

    /**
     * 비동기적으로 이미지 삭제
     */
    @DeleteMapping("/delete")
    public CompletableFuture<ResponseEntity<String>> deleteImage(@RequestParam("fileName") String fileName) {
        return s3AsyncService.deleteFile(fileName)
                .thenApply(voidRes -> ResponseEntity.ok("File deleted successfully: " + fileName))
                .exceptionally(ex -> ResponseEntity.badRequest().body("File delete failed: " + ex.getMessage()));
    }
}