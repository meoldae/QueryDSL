package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJPARepository;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MemberControllerV1 {

    private final MemberJPARepository memberJPARepository;

    @GetMapping("/members")
    public ResponseEntity<List<MemberTeamDto>> memberSearchV1(MemberSearchCondition condition) {
        log.info("[[V1 Members Called]]");
        List<MemberTeamDto> members = memberJPARepository.searchMemberTeamDtoByCondition(condition);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }
}
