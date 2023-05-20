package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();
        // find 메서드의 인자로 member.getId()를 리턴해도 결과 동일

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long reservedCount = memberRepository.count();
        assertThat(reservedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findByUsername() {
        Member member1 = new Member("minjeong", 10);
        Member member2 = new Member("minjeong", 20);
        Member member3 = new Member("minjeong", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findByUsername("minjeong");
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("findUser - @Query 리포지토리 메서드에 정의한 JPQL 쿼리 테스트")
    public void findUserTest() {
        Member member1 = new Member("minjeong", 10);
        Member member2 = new Member("minjeong", 20);
        Member member3 = new Member("minjeong", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<Member> result = memberRepository.findUser("minjeong", 20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findUsernameListTest() {
        Member member1 = new Member("minjeong", 10);
        Member member2 = new Member("minjeong", 20);
        Member member3 = new Member("minjeong", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        List<String> usernameList = memberRepository.findUsernameList();
        List<String> distinctList = usernameList.stream().distinct().toList();
        assertThat(distinctList.get(0)).isEqualTo("minjeong");
        assertThat(distinctList.size()).isEqualTo(1);
    }

    @Test
    public void findMemberDtoTest() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("memberA", 10);
        member.setTeam(team);
        memberRepository.save(member);

        List<MemberDto> memberDtos = memberRepository.findMemberDto();
        assertThat(memberDtos.get(0).getUsername()).isEqualTo(member.getUsername());
        assertThat(memberDtos.get(0).getTeamName()).isEqualTo(team.getName());
    }

    @Test
    void findMembersTest() {
        List<String> userNameList = Arrays.asList("memberA", "memberB", "memberC");
        userNameList.forEach(userName -> memberRepository.save(new Member(userName, 10)));

        Member memberA = memberRepository.findMembers("memberA");
        assertThat(memberA.getUsername()).isEqualTo("memberA");
        assertThat(memberA.getAge()).isEqualTo(10);
    }

    @Test
    public void findByNamesTest() {
        List<String> userNameList = Arrays.asList("memberA", "memberB", "memberC");
        userNameList.forEach(userName -> memberRepository.save(new Member(userName, 10)));

        List<Member> members = memberRepository.findByNames(userNameList);
        for (Member member : members) {
            System.out.println("member = " + member);
            assertThat(member.getAge()).isEqualTo(10);
        }
    }

    @Test
    @DisplayName("단건 조회 시 조회 결과 없을 경우 null 리턴")
    void findMembersNoResultTest() {
        Member member1 = new Member("minjeong", 10);
        Member member2 = new Member("minjeong", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회시 결과가 없을 경우 JPA는 javax.persistence.NoResultException 예외를 던짐
        // JPA가 발생시킨 예외를 Spring 데이터 JPA가 무시하고 null로 반환
        assertThat(memberRepository.findMembers("limminjeong")).isEqualTo(null);
    }

    @Test
    @DisplayName("단건 조회에서 2개 이상의 결과 리턴될 경우 예외 발생")
    void findOptionalByUsernameTest() {
        Member member1 = new Member("minjeong", 10);
        Member member2 = new Member("minjeong", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // JPA에서 발생시킨 예외를 Spring 데이터 JPA가 스프링 예외로 추상화해서 던짐
        // NonUniqueResultException -> IncorrectResultSizeDataAccessException
        assertThatThrownBy(
                () -> memberRepository.findOptionalByUsername("minjeong")
        ).isInstanceOf(IncorrectResultSizeDataAccessException.class);

        assertThatThrownBy(
                () -> memberRepository.findOptionalByUsername("minjeong")
        ).isNotInstanceOf(NonUniqueResultException.class);
    }

    @Test
    @DisplayName("페이징 조건과 정렬 조건 설정")
    void page() {
        // given
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 10, teamA));
        memberRepository.save(new Member("member3", 10, teamA));
        memberRepository.save(new Member("member4", 10, teamA));
        memberRepository.save(new Member("member5", 10, teamA));

        // when
        // PageRequest는 Pageable의 구현체
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        // 메서드 실행 결과에 대한 반환 타입을 Page로 정의하면 스프링 데이터 JPA가 알아서 카운트 쿼리 날림

        // then
        // page = 전체 데이터, content = 조회된 데이터
        List<Member> content = page.getContent();                       // 조회된 데이터
        assertThat(content.size()).isEqualTo(3);               // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5);      // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0);             // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);         // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue();                            // 첫 페이지인가
        assertThat(page.hasNext()).isTrue();                            // 다음 페이지가 있는가

        // Json 응답 시 반환 타입은 Entity -> DTO로 변환 필수
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName()));
    }

    @Test
    void bulkUpdate() {
        // given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        Member member3 = new Member("member3", 30);
        Member member4 = new Member("member4", 40);
        Member member5 = new Member("member5", 50);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        // then
        assertThat(resultCount).isEqualTo(4);

        // @Modifying의 clearAutomatically를 true로 지정해주지 않으면 기본 false
        // 영속성 컨텍스트에 남아있는 엔티티의 상태와 DB에 벌크 연산이 수행된 이후의 엔티티의 상태가 다름
        // 이 옵션 주지 않으면서 정상적으로 조회하려면 아래 2개 쿼리 실행 후 조회 필요
        // em.flush();
        // em.clear();
        Member findMember = memberRepository.findMembers("member5");
        // clearAutomatically 옵션과 상관 없이 member5의 나이는 업데이트되지 않음에 주의
        // clearAutomatically = false이면 에러
        assertThat(findMember.getAge()).isEqualTo(51);
    }

    @Test
    void findMemberLazy() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamB);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        em.flush();
        em.clear();

        // when - 1
        List<Member> members = memberRepository.findAll();

        // then
        // 지연 로딩으로 인한 N + 1 문제 발생
        // Team 엔티티는 프록시 객체로 생성되어 실제 호출될 때 조회 쿼리 실행되고 초기화됨
        System.out.println("members = " + members);
        for (Member member : members) {
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        em.flush();
        em.clear();

        // when - 2
        // 페치 조인으로 N + 1 문제 해결
        List<Member> members2 = memberRepository.findMemberFetchJoin();

        // then
        // 페치 조인으로 한 번에 조회하여 지연 로딩 X. N + 1 문제 발생 안 함
        // Team 엔티티 프록시 객체로 생성되지 않음
        System.out.println("members2 = " + members2);
        for (Member member : members2) {
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        // when - 3
        // @EntityGraph 사용하여 N + 1 문제 해결
        // EntityGraph는 LEFT OUTER JOIN 사용하여 JPQL 없이도 페치 조인 적용 가능
        List<Member> members3 = memberRepository.findMemberEntityGraph();

        // EntityGraph 적용하여 지연 로딩 X. N + 1 문제 발생 안 함
        // Team 엔티티 프록시 객체로 생성되지 않음
        System.out.println("members3 = " + members3);
        for (Member member : members3) {
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }

        // when - 4
        // 엔티티에 정의한 @NamedEntityGraph를 리포지토리 메서드의 @EntityGraph에서 사용
        List<Member> members4 = memberRepository.findEntityGraphByUsername("memberA");

        // EntityGraph 적용하여 지연 로딩 X. N + 1 문제 발생 안 함
        // Team 엔티티 프록시 객체로 생성되지 않음
        System.out.println("members4 = " + members4);
        for (Member member : members4) {
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        // 플러시 호출 시점에 변경 감지 동작하여 insert, update 쿼리 등이 DB로 전달되어 실행됨
        // 여기서는 읽기 전용 객체로 가져왔기 때문에, 스냅샷 객체 생성 안하고 변겸 감지 안 하도록 최적화됨
        // 플러시 호출해도 update 쿼리 실행 안 됨
        em.flush();
        em.clear();

        assertThat(memberRepository.findMembers("member2")).isEqualTo(null);
    }

    @Test
    void lock() {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        // when
        // select ~ from ~ where ~ for update 라고 쿼리가 나감
        List<Member> members = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void callCustom() {
        // MemberRepository에 MemberRepositoryCustom(사용자 정의 인터페이스) 구현하겠다고 정의하면
        // 사용자 정의 인터페이스를 구현한 MemberRepositoryCustomImpl(구현체)의 메서드를 실행
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    void testSpecificationBasic() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void testQueryByExample() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        // Probe(필드에 데이터가 있는 실제 도메인 객체) 생성
        Member member = new Member("m1");
        Team team = new Team("teamA"); // 내부조인으로 teamA 가능
        member.setTeam(team);

        // ExampleMatcher 생성, age 프로퍼티는 무시 (primitive type이라 기본값 0으로 세팅되어서)
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);

        // then
        assertThat(result.size()).isEqualTo(1);
    }
}