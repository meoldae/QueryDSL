package study.querydsl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

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

    @Test
    public void basicTest() {
        String username = "Member1";
        int age = 10;

        Member member1 = new Member(username,  age);
        memberJPARepository.save(member1);

        Optional<Member> findMember = memberJPARepository.findById(member1.getId());

        findMember.ifPresentOrElse(
                (member) -> assertThat(member).isEqualTo(member1),
                () -> new RuntimeException("Member Not Saved !!!")
        );

        List<Member> result = memberJPARepository.findAll();
        assertThat(result).containsExactly(member1);

        List<Member> result2 = memberJPARepository.findByUsername(username);
        assertThat(result2).containsExactly(member1);
    }

    @Test
    public void basicQueryDslTest() {
        String username = "Member1";
        int age = 10;

        Member member1 = new Member(username,  age);
        memberJPARepository.save(member1);

        Optional<Member> findMember = memberJPARepository.findByIdQueryDsl(member1.getId());

        findMember.ifPresentOrElse(
                (member) -> assertThat(member).isEqualTo(member1),
                () -> new RuntimeException("Member Not Saved !!!")
        );

        List<Member> result = memberJPARepository.findAllQueryDsl();
        assertThat(result).containsExactly(member1);

        List<Member> result2 = memberJPARepository.findByUsernameQueryDsl(username);
        assertThat(result2).containsExactly(member1);
    }
}