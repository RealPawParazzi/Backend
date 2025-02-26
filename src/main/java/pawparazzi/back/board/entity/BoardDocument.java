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
    private String id;

    @Indexed(unique = true)
    private Long mysqlId;

    private String title;

    private String titleImage;

    private String titleContent;

    private List<ContentDto> contents;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }

    public BoardDocument(Long mysqlId, String title, String titleImage, String titleContent, List<ContentDto> contents) {
        this.mysqlId = mysqlId;
        this.title = title;
        this.titleImage = titleImage;
        this.titleContent = titleContent;
        this.contents = contents;
    }
}