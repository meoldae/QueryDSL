package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
//@Commit
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void entityTest(){
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamA");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("Member1", 15, teamA);
        Member member2 = new Member("Member2", 20, teamA);
        Member member3 = new Member("Member3", 25, teamB);
        Member member4 = new Member("Member4", 30, teamB);

        List<Member> oldMembers = new ArrayList<>();
        oldMembers.add(member1);
        oldMembers.add(member2);
        oldMembers.add(member3);
        oldMembers.add(member4);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("SELECT m FROM Member m", Member.class).getResultList();

        for (int i = 0; i < members.size(); i++) {
            Member oldMember = oldMembers.get(i);
            Member member = members.get(i);
            assertThat(oldMember.getUsername()).isEqualTo(member.getUsername());
            assertThat(oldMember.getAge()).isEqualTo(member.getAge());
            assertThat(oldMember.getTeam().getTeamName()).isEqualTo(member.getTeam().getTeamName());
        }

    }

}