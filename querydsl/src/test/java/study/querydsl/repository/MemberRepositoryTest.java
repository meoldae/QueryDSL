package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        String username = "Member5";
        int age = 10;

        Member member1 = new Member(username,  age);
        memberRepository.save(member1);

        Optional<Member> findMember = memberRepository.findById(member1.getId());

        findMember.ifPresentOrElse(
                (member) -> assertThat(member).isEqualTo(member1),
                () -> new RuntimeException("Member Not Saved !!!")
        );

        List<Member> result = memberRepository.findAll();
        assertThat(result).contains(member1);

        List<Member> result2 = memberRepository.findByUsername(username);
        assertThat(result2).containsExactly(member1);
    }

}