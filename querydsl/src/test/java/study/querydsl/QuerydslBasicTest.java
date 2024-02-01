package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

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
        Team teamB = new Team("TeamB");
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
                .where(member.username.eq("Member1"))
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

    @Test
    public void paging1() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .orderBy(member.username.asc().nullsLast())
                .offset(1) // N 번째 부터, 단 offset은 0부터 시작함을 명심하자.
                .limit(2) // 시작 지점부터 N 개의 결과
                .fetch();

        assertThat(fetch.size()).isEqualTo(2);

        Member member2 = fetch.get(0);
        Member member3 = fetch.get(1);

        assertThat(member2.getUsername()).isEqualTo("Member2");
        assertThat(member3.getUsername()).isEqualTo("Member3");
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.asc().nullsLast())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getResults().size()).isEqualTo(2);

        // 하지만 fetchResults는 Deprecated 된 API 이므로 totalCount는 다음의 구문을 사용하자.
        Long count = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        assertThat(count).isEqualTo(4);
    }

    @Test
    public void aggregation() {
        Tuple tuple = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetchOne();

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(90);
        assertThat(tuple.get(member.age.avg())).isEqualTo(22.5);
        assertThat(tuple.get(member.age.max())).isEqualTo(30);
        assertThat(tuple.get(member.age.min())).isEqualTo(15);
    }

    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(
                        team.teamName,
                        member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.teamName)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.teamName)).isEqualTo("TeamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(17.5);
        assertThat(teamB.get(team.teamName)).isEqualTo("TeamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(27.5);

    }

    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.teamName.eq("TeamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("Member1", "Member2");
    }

    /**
     * 모든 Member, Team 테이블 값을 Join 한 후 조건 적용.
     * DB 최적화가 되긴 하나, 사용하는 DB 별로 다름.
     * 외부 join을 사용하려면 join on 절을 사용해야 함.
     */
    @Test
    public void thetaJoin() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.teamName))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("TeamA", "TeamB");
    }

    /**
     *  회원, 팀을 조인하면서 팀 이름이 "TeamA"인 팀만 조회하라.
     *  JPQL : SELECT m, t FROM Member m left join m.team t ON t.name = 'TeamA'
     */
    @Test
    public void joinOnFiltering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.teamName.eq("TeamA"))
                .fetch();

    }

    /**
     * 연관관계 없는 테이블 간 외부 조인
     */
    @Test
    public void outerJoinOnNoRelation() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.teamName))
                .fetch();

        for (Tuple t : result) {
            System.out.println("t = " + t);
        }
    }
}