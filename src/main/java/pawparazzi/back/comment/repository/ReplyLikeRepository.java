package pawparazzi.back.comment.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pawparazzi.back.comment.entity.Reply;
import pawparazzi.back.comment.entity.ReplyLike;
import pawparazzi.back.member.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
    Optional<ReplyLike> findByReplyAndMember(Reply reply, Member member);
    List<ReplyLike> findByReply(Reply reply);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReplyLike rl WHERE rl.reply.id = :replyId")
    void deleteByReplyId(Long replyId);
}