package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        // 도메인 클래스 컨버터도 리포지토리를 사용해서 엔티티를 찾음
        // 단순 조회용으로만 사용해야 한다
        return member.getUsername();
    }

    @GetMapping("/members-old")
    public Page<Member> list(@PageableDefault(size = 5, sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    // 페이징 정보가 둘 이상이면 접두사로 구분 (@Qualifier에 접두사명 추가)
    // /members?member_page=0&order_page=1
    // @GetMapping("/members")
    // public String list(@Qualifier("member") Pageable memberPageable,
    //                    @Qualifier("order") Pageable orderPageable) {
    //     return ...;
    // }

    @GetMapping("/members")
    public Page<MemberDto> listOfPageDto(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page.map(MemberDto::new);
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
