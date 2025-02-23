package pawparazzi.back.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pawparazzi.back.board.dto.PostBoardRequestDto;
import pawparazzi.back.board.dto.PostBoardResponseDto;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.entity.Image;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.board.repository.ImageRepository;
import pawparazzi.back.common.Dto.ResponseDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImplement implements BoardService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final ImageRepository imageRepository;

    @Override
    public ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, Long id) {

        try {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
            Board board = new Board(dto, member);
            boardRepository.save(board);

            List<String> boardImageList = dto.getBoardImageList();
            List<Image> imageEntities = new ArrayList<>();

            for (String imageUrl : boardImageList) {
                Image imageEntity = new Image(board, imageUrl);
                imageEntities.add(imageEntity);
            }

            imageRepository.saveAll(imageEntities);

        } catch(Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return PostBoardResponseDto.success();
    }
}
