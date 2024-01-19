package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;
    // Spring 에서 동시성 문제가 발생하지 않도록 설계되어 있으니 필드로 사용 권장

    @BeforeEach
    public void setTestCase(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamA");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("Member1", 15, teamA);
        Member member2 = new Member("Member2", 20, teamA);
        Member member3 = new Member("Member3", 25, teamB);
        Member member4 = new Member("Member4", 30, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();
    }

    @Test
    public void JPQLTest(){
        // Find Member Where username = Member1
        Member findMember = em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class)
                .setParameter("username", "Member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("Member1");
    }

    @Test
    public void QueryDSLTest(){
//        QMember m = new QMember("m");
        // ↑ 같은 테이블을 Join 해야 할 때 alias를 다르게 하기 위해 사용함
        QMember m = QMember.member;

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("Member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("Member1");
    }

}
