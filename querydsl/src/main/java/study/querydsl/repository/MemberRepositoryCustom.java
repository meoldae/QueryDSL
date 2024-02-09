package study.querydsl.repository;

import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

@Repository
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);
}
