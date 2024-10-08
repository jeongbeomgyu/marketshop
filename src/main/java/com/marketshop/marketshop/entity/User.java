package com.marketshop.marketshop.entity;

import com.marketshop.marketshop.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEntity{

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column
    private String picture;

    private String provider;    // oauth2를 이용할 경우 어떤 플랫폼을 이용하는지
    private String providerId;  // oauth2를 이용할 경우 아이디값

    @Enumerated(EnumType.STRING)
    @Setter
    private Role role;

    @Builder(builderClassName = "UserDetailRegister", builderMethodName = "userDetailRegister")
    public User(String name, String email, String password, String picture, Role role, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.picture = picture;
        this.role = role;
    }

    @Builder(builderClassName = "OAuth2Register", builderMethodName = "oauth2Register")
    public User(String name, String email, Role role, String provider, String providerId) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    public User(String username, String password, List<GrantedAuthority> authorities) {
        super();
    }

    public Long getId() { return id; }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() { return address; }

    public String getPicture() {
        return picture;
    }
}
