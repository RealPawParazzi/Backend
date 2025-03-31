package pawparazzi.back.pet.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PetImageHelper {

    private static final String DEFAULT_IMAGE_URL = "https://your-bucket.s3.region.amazonaws.com/pet_images/default_pet.png";
    private static final String IMAGE_FOLDER = "pet_images/";

    /**
     * S3에 저장될 경로 prefix 생성
     */
    public static String getPathPrefix(String nickname) {
        return IMAGE_FOLDER + encode(nickname) + "/";
    }

    /**
     * 기본 이미지 URL 반환
     */
    public static String getDefaultImageUrl() {
        return DEFAULT_IMAGE_URL;
    }

    /**
     * URL에서 파일명 추출
     */
    public static String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    /**
     * 경로에 사용할 닉네임 인코딩
     */
    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}