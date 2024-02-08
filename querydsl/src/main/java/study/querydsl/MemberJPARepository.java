package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberJPARepository {
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

//    Bean으로 QueryFactory를 등록했으므로, 의존성 주입만 받음
//    public MemberJPARepository(EntityManager em) {
//        this.em = em;
//        this.jpaQueryFactory = new JPAQueryFactory(em);
//    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public Optional<Member> findByIdQueryDsl(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(member)
                .where(member.id.eq(id))
                .fetchOne());
    }

    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM Member m", Member.class).getResultList();
    }

    public List<Member> findAllQueryDsl() {
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class)
                .setParameter("username", username )
                .getResultList();
    }

    public List<Member> findByUsernameQueryDsl(String username) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
}
