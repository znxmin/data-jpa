package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // @Query 생략하고 메서드 이름만으로 Named 쿼리 호출 가능 (메서드 이름으로 쿼리 생성 전략 사용)
    // @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age > :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
}
