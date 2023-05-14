package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

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
}