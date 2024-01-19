package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

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
        QMember m = member;

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("Member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("Member1");
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("Member1")
                        .and(member.age.eq(15)))
                .fetchOne();

                /* 여러 조건 구문
                .where(member.username.eq("Member1")

                .where(member.username.ne("Member1")
                .where(member.username.ne("Member1").not()

                .where(member.username.isNotNull()

                .where(member.age.in(10, 20))
                .where(member.age.notIn(10, 20))
                .where(member.age.between(10, 20)

                .where(member.age.goe(10))
                .where(member.age.gt(10))
                .where(member.age.loe(10))
                .where(member.age.lt(10))

                .where(member.username.like("Member%")        // Member%
                .where(member.username.contains("Member")     // %Member%
                .where(member.username.startsWith("Member")   // Member%
                .where(member.username.endsWith("Member")     // %Member
                 */

    assertThat(findMember.getUsername()).isEqualTo("Member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("Member1"),
                        member.age.eq(15)
                )
                .fetchOne();
                // ',' 쉼표 사용해도 and() 로

        assertThat(findMember.getUsername()).isEqualTo("Member1");
    }

    @Test
    public void fetchTest() {
        List<Member> fetchList = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();
        // limit(1).fetchOne() 과 동일

        /*
        QueryResults<Member> memberQueryResults = queryFactory.
                selectFrom(member)
                .fetchResults();

        long count = queryFactory
                .selectFrom(member)
                .fetchCount();

        fetchResults 는 복잡한 Query 에서 예외가 발생하므로 fetch를 사용하고 limit, offset 등을 사용하고
        전체 수가 필요하면 count 쿼리를 직접 작성하여 사용하자!
        현재 Deprecated 된 API
         */

    }

    /*
    정렬 조건
    1. 나이 내림차순
    2. 이름 오름차순
    3. 이름이 Null이라면 마지막에 출력
     */
    @Test
    public void sort() {
        em.persist(new Member("Member5", 100));
        em.persist(new Member("Member6", 100));
        em.persist(new Member(null, 100));

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                )
                .fetch();

        Member member5 = fetch.get(0);
        Member member6 = fetch.get(1);
        Member memberNull = fetch.get(2);

        assertThat(member5.getUsername()).isEqualTo("Member5");
        assertThat(member6.getUsername()).isEqualTo("Member6");
        assertThat(memberNull.getUsername()).isNull();

    }
}