package pawparazzi.back.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pawparazzi.back.exception.AccessDeniedException;
import pawparazzi.back.inquiry.dto.InquiryDetailResponse;
import pawparazzi.back.inquiry.dto.InquiryRequest;
import pawparazzi.back.inquiry.dto.InquiryListResponse;
import pawparazzi.back.inquiry.entity.Inquiry;
import pawparazzi.back.inquiry.repository.InquiryRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    public void createInquiry(InquiryRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Inquiry inquiry = Inquiry.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .build();

        inquiryRepository.save(inquiry);
    }

    public List<InquiryListResponse> getMyInquiries(Long memberId) {
        return inquiryRepository.findAllByMemberId(memberId).stream()
                .map(inquiry -> InquiryListResponse.builder()
                        .id(inquiry.getId())
                        .title(inquiry.getTitle())
                        .answered(inquiry.isAnswered())
                        .createdAt(inquiry.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }


    public InquiryDetailResponse getInquiryDetail(Long inquiryId, Long memberId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));

        if (!inquiry.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("본인이 작성한 문의글만 조회할 수 있습니다.");
        }

        return InquiryDetailResponse.from(inquiry);
    }

    public void deleteInquiry(Long inquiryId, Long memberId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));

        if (!inquiry.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("본인이 작성한 문의글만 삭제할 수 있습니다.");
        }

        inquiryRepository.delete(inquiry);
    }
}
