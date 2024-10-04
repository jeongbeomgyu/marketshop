package com.marketshop.marketshop.entity;

import com.marketshop.marketshop.constant.Role;
import com.marketshop.marketshop.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity implements UserDetails {   // 회원 정보 저장 엔티티

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String originalpassword;

    private String password;

    private String address;

    private String streetaddress;

    private String detailaddress;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider;    // oauth2 를 이용할 경우 어떤 플랫폼을 이용하는지

    private String providerId;  // oauth2 를 이용할 경우 아이다 값

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getZipcode());
        member.setDetailaddress(memberFormDto.getDetailadr());
        member.setStreetaddress(memberFormDto.getStreetadr());
        member.setOriginalpassword(memberFormDto.getPassword());

        // Spring Security Config 에서 설정 클래스로 등록한 Bean 을 파라미터로 넘겨서 비밀번호 암호화
        String password = passwordEncoder.encode(memberFormDto.getPassword());

        member.setPassword(password);
        member.setRole(Role.USER);
        return member;
    }

    public static Member createAdminMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {

        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getZipcode());
        member.setDetailaddress(memberFormDto.getDetailadr());
        member.setStreetaddress(memberFormDto.getStreetadr());
        member.setOriginalpassword(memberFormDto.getPassword());

        // Spring Security Config 에서 설정 클래스로 등록한 Bean 을 파라미터로 넘겨서 비밀번호 암호화
        String password = passwordEncoder.encode(memberFormDto.getPassword());

        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }

    public Member updateModifiedDate() {
        this.onPreUpdate();
        return this;
    }

    @Builder(builderClassName = "UserDetailRegister", builderMethodName = "userDetailRegister")
    public Member(String name, String password, String email, Role role) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    @Builder(builderClassName = "OAuth2Register", builderMethodName = "oauthRegister")
    public Member(String name, String password, String email, Role role, String provider, String providerId) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    // 회원수정 메서드
    public void updateUsername(String name) {
        this.name = name;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateOriginalPassword(String originalpassword) {
        this.originalpassword = originalpassword;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateStreetAddress(String streetaddress) {
        this.streetaddress = streetaddress;
    }

    public void updateDetailAddress(String detailaddress) {
        this.detailaddress = detailaddress;
    }
}
