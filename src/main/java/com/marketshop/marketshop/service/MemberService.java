package com.marketshop.marketshop.service;

import com.marketshop.marketshop.dto.LoginRequest;
import com.marketshop.marketshop.dto.MemberUpdateDto;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;



    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        // 비밀번호 암호화 후 저장
        return memberRepository.save(member);
    }

    // 이미 가입된 회원인 경우 예외 처리
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if ((findMember != null)) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }

    }
    // 비밀번호 일치 확인
    @ResponseBody
    public boolean checkPassword(Member member, String checkPassword) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember == null) {
            throw new IllegalStateException("없는 회원입니다.");
        }
        String realPassword = member.getPassword();
        boolean matches = passwordEncoder.matches(checkPassword, realPassword);
        System.out.println(matches);
        return matches;
    }

    // 회원정보 수정
    public Long updateMember(MemberUpdateDto memberUpdateDto) {
        Member member = memberRepository.findByEmail(memberUpdateDto.getEmail());
        member.updateUsername(memberUpdateDto.getName());
        member.updateAddress(memberUpdateDto.getZipcode());
        member.updateStreetAddress(memberUpdateDto.getStreetadr());
        member.updateDetailAddress(memberUpdateDto.getDetailadr());
        member.updateOriginalPassword(memberUpdateDto.getPassword());

        // 회원 비밀번호 수정을 위한 패스워드 암호화
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodePw = encoder.encode(memberUpdateDto.getPassword());
        member.updatePassword(encodePw);

        memberRepository.save(member);

        return member.getId();
    }

    public Member getUserByNumber(Long userNumber){

        return memberRepository.findById(userNumber).orElse(null);
    }

    // 로그인 검증만 처리
    public UserDetails authenticateUser(LoginRequest loginRequest) throws Exception {
        // 사용자의 이메일로 UserDetails 로드
        UserDetails userDetails = loadUserByUsername(loginRequest.getEmail());

        // 비밀번호가 맞는지 확인 (암호화된 비밀번호와 비교)
        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            log.error("비밀번호가 일치하지 않습니다. 요청된 비밀번호: {}", loginRequest.getPassword());
            log.info("저장된 비밀번호: {}", userDetails.getPassword());
            log.info("입력된 비밀번호: {}", loginRequest.getPassword());
            throw new Exception("Invalid credentials");
        }

        return userDetails;
    }

    // 유저 로그인
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public Member findByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("해당 이메일의 회원을 찾을 수 없습니다.");
        }
        return member;
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() ->
                new IllegalArgumentException("해당 아이디의 회원을 찾을 수 없습니다.")
        );
    }

}
