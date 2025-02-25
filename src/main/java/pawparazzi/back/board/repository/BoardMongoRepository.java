package pawparazzi.back.board.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pawparazzi.back.board.entity.BoardDocument;

import java.util.List;
import java.util.Optional;

public interface BoardMongoRepository extends MongoRepository<BoardDocument, String> {
    Optional<BoardDocument> findByMysqlId(Long mysqlId);
    void deleteByMysqlId(Long mysqlId);
}