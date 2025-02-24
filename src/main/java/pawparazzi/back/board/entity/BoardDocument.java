package pawparazzi.back.board.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Document(collection = "boards")
@Getter
@Setter
@NoArgsConstructor
public class BoardDocument {

    @Id
    private String id;  // MongoDB ObjectId

    @Indexed(unique = true)  // MySQL ID로 조회 가능하도록 인덱싱
    private Long mysqlId;  // MySQL의 게시글 ID

    private String title;  // 게시글 제목

    private String titleImage;  // 대표 이미지

    private String titleContent;  // 대표 텍스트 (본문 요약)

    private List<ContentDto> contents;  // 본문 내용 (텍스트, 이미지, 동영상)

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;  // "text" | "image" | "video"
        private String value; // 본문 데이터
    }

    public BoardDocument(Long mysqlId, String title, String titleImage, String titleContent, List<ContentDto> contents) {
        this.mysqlId = mysqlId;
        this.title = title;
        this.titleImage = titleImage;
        this.titleContent = titleContent;
        this.contents = contents;
    }
}