package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;
import study.querydsl.repository.MemberJPARepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJPARepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJPARepository memberJPARepository;

    @BeforeEach
    public void setTestCase(){
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("Member1", 10, teamA);
        Member member2 = new Member("Member2", 20, teamA);
        Member member3 = new Member("Member3", 30, teamB);
        Member member4 = new Member("Member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();
    }

    @Test
    public void basicTest() {
        String username = "Member5";
        int age = 10;

        Member member1 = new Member(username,  age);
        memberJPARepository.save(member1);

        Optional<Member> findMember = memberJPARepository.findById(member1.getId());

        findMember.ifPresentOrElse(
                (member) -> assertThat(member).isEqualTo(member1),
                () -> new RuntimeException("Member Not Saved !!!")
        );

        List<Member> result = memberJPARepository.findAll();
        assertThat(result).contains(member1);

        List<Member> result2 = memberJPARepository.findByUsername(username);
        assertThat(result2).containsExactly(member1);
    }

    @Test
    public void basicQueryDslTest() {
        String username = "Member5";
        int age = 10;

        Member member1 = new Member(username,  age);
        memberJPARepository.save(member1);

        Optional<Member> findMember = memberJPARepository.findByIdQueryDsl(member1.getId());

        findMember.ifPresentOrElse(
                (member) -> assertThat(member).isEqualTo(member1),
                () -> new RuntimeException("Member Not Saved !!!")
        );

        List<Member> result = memberJPARepository.findAllQueryDsl();
        assertThat(result).contains(member1);

        List<Member> result2 = memberJPARepository.findByUsernameQueryDsl(username);
        assertThat(result2).containsExactly(member1);
    }

    @Test
    public void searchTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("TeamB");

        List<MemberTeamDto> result = memberJPARepository.searchMemberTeamDtoByCondition(condition);
        assertThat(result).extracting("username").containsExactly("Member4");

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition.setTeamName("TeamB");

        List<MemberTeamDto> result2 = memberJPARepository.searchMemberTeamDtoByCondition(condition2);
        assertThat(result2).extracting("username").contains("Member3", "Member4");

    }

    @Test
    public void searchParameterTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("TeamB");

        List<MemberTeamDto> result = memberJPARepository.searchMemberTeamDtoByParameter(condition);
        assertThat(result).extracting("username").containsExactly("Member4");

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition.setTeamName("TeamB");

        List<MemberTeamDto> result2 = memberJPARepository.searchMemberTeamDtoByParameter(condition2);
        assertThat(result2).extracting("username").contains("Member3", "Member4");

    }
}