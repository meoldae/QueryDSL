package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslIntermediateTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

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
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("SELECT new study.querydsl.dto.MemberDto(m.username, m.age) FROM Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDto() {
        List<UserDto> result = queryFactory
                .select(Projections.fields(
                        UserDto.class, member.username.as("name"), member.age
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }

        // username != name 이므로 매칭되는게 없어 userDto의 name은 null값이 들어간다.
        // as를 통해 별칭을 설정하여 정상 작동
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

        /**
         * 왜 Projections.constructor 말고 QueryProjection을 쓰는가 ?
         * Projections.constructor은 파라미터의 타입이 잘못되어도 컴파일 시점에 오류를 검출하지 못하고 런타임에 오류가 발생한다.
         * 다만, Q객체를 생성해야하고 Dto에 QueryDSL에 대한 의존성을 갖게 된다.
         */
    }

    @Test
    public void dynamicQueryByBooleanBuilder() {
        String usernameParam = "Member1";
        Integer ageParam = 15;

        List<Member> result = searchMemberByParam1(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    public List<Member> searchMemberByParam1(String usernameParam, Integer ageParam) {

        BooleanBuilder builder = new BooleanBuilder();
        // 생성 시 초기 조건 설정도 가능
        // BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameParam));

        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory.select(member)
                .where(builder)
                .from(member)
                .fetch();
    }

    @Test
    public void dynamicQueryByWhereParam() {
        String usernameParam = "Member1";
        Integer ageParam = 15;

        List<Member> result = searchMemberByParam2(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    public List<Member> searchMemberByParam2(String usernameParam, Integer ageParam) {
        return queryFactory.selectFrom(member)
//                .where(usernameEq(usernameParam), ageEq(ageParam))
                .where(combinedEq(usernameParam, ageParam))
                .fetch();
    }

//    private Predicate usernameEq(String usernameParam) {
//        return usernameParam != null ? member.username.eq(usernameParam) : null;
//    }
//
//    private Predicate ageEq(Integer ageParam) {
//        return ageParam != null ? member.age.eq(ageParam) : null;
//    }

     // BooleanExpression을 사용하면 BooleanBuilder처럼 조립도 가능
    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression combinedEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

    @Test
    public void bulkUpate() {

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(25))
                .execute();

        assertThat(count).isEqualTo(2);

        /**
         * 주의 !
         * 벌크 연산은 영속성 컨텍스트에 영향을 끼치지 않고 DB에 직접 관여하므로 괴리가 발생
         * 조회시 DB에서 쿼리는 날아가지만, 영속성 컨텍스트에서 1차 캐시가 이미 있다고 판단하여 변경되기 이전의 값을 반환한다.
         * 때문에 flush, clear를 수행하여 DB와 영속성 컨텍스트의 컨디션을 맞추자.
         */

        em.flush();
        em.clear();
    }

    @Test
    public void bulkAdd() {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void bulkDelete() {
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }
}
