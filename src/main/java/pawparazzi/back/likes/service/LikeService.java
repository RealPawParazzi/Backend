package pawparazzi.back.likes.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.likes.dto.LikeMemberDto;
import pawparazzi.back.likes.dto.LikeResponseDto;
import pawparazzi.back.likes.entity.Like;
import pawparazzi.back.likes.repository.LikeRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    /**
     * 좋아요 등록/삭제 (토글)
     */
    @Transactional
    public Map<String, Object> toggleLike(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 좋아요 여부 확인
        Like existingLike = likeRepository.findByBoardAndMember(board, member).orElse(null);

        boolean liked;
        if (existingLike != null) {
            likeRepository.delete(existingLike);
            board.decreaseFavoriteCount();
            liked = false;
        } else {
            likeRepository.save(new Like(board, member));
            board.increaseFavoriteCount();
            liked = true;
        }

        return Map.of("liked", liked, "favoriteCount", board.getFavoriteCount());
    }

    /**
     * 특정 게시글의 좋아요 누른 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public LikeResponseDto getLikesByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        List<LikeMemberDto> likedUsers = likeRepository.findByBoard(board).stream()
                .map(like -> new LikeMemberDto(
                        like.getMember().getId(),
                        like.getMember().getNickName(),
                        like.getMember().getProfileImageUrl()
                ))
                .collect(Collectors.toList());

        return new LikeResponseDto(board.getId(), board.getFavoriteCount(), likedUsers);
    }
}