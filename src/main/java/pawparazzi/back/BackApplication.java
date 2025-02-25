package pawparazzi.back;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackApplication {
    public static void main(String[] args) {
        // .env 파일 로드
        Dotenv dotenv = Dotenv.load();
        System.setProperty("PAWPARAZZI_DB_URL", dotenv.get("PAWPARAZZI_DB_URL"));
        System.setProperty("PAWPARAZZI_DB_USERNAME", dotenv.get("PAWPARAZZI_DB_USERNAME"));
        System.setProperty("PAWPARAZZI_DB_PASSWORD", dotenv.get("PAWPARAZZI_DB_PASSWORD"));
        System.setProperty("PAWPARAZZI_MONGO_URI", dotenv.get("PAWPARAZZI_MONGO_URI"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));

        SpringApplication.run(BackApplication.class, args);
    }
}
