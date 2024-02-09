package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJPARepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
public class MemberControllerV1 {

    private final MemberJPARepository memberJPARepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public ResponseEntity<List<MemberTeamDto>> memberSearchV1(MemberSearchCondition condition) {
        List<MemberTeamDto> members = memberJPARepository.searchMemberTeamDtoByCondition(condition);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    @GetMapping("/v2/members")
    public ResponseEntity<Page<MemberTeamDto>> memberSearchV2(MemberSearchCondition condition, Pageable pageable) {
        Page<MemberTeamDto> members = memberRepository.searchPageComplex(condition, pageable);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }
}
