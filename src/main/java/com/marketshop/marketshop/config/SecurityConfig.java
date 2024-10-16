package com.marketshop.marketshop.config;

import com.marketshop.marketshop.config.auth.PrincipalOauth2UserService;
import com.marketshop.marketshop.service.MemberService;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final UserDetailsService userDetailsService;

    private final PrincipalOauth2UserService principalOauth2UserService;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;




    // React 와 연동 가능하도록 SecurityFilterChain 변경
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // REST API 기반으로 Form 로그인 비활성화

                // OAuth2 로그인 설정 (JSON 응답 기반)
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/")  // 성공 후 리디렉션 URL
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(principalOauth2UserService) // OAuth2 사용자 정보 서비스 설정
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll() // 정적 리소스 허용
                        // Swagger 관련 URL들은 인증 없이 접근 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/", "/members/**", "/item/**", "/images/**", "/mail/**", "/search/**","/error").permitAll()
                        .requestMatchers("/ws/**").permitAll()  // WebSocket 경로를 허용
                        .requestMatchers("/chat/**").authenticated()// 이 경로는 인증된 사용자만 허용
                        .requestMatchers("/admin/**").hasRole("USER") // ADMIN 권한을 가진 사용자만 접근 가능
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )

                // CSRF 설정 (REST API 환경에서 비활성화)
                .csrf(csrf -> csrf.disable()
                        // ignoringRequestMatchers("/mail/**", "/members/findId") // 특정 경로에 대해 CSRF 비활성화
                )

                // 예외 처리 (인증되지 않은 사용자를 처리하기 위해 Custom EntryPoint 사용)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증되지 않은 사용자 처리
                )

                // 세션 관리 정책 설정 (REST API에서는 STATELESS가 일반적)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 생성하지 않음
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        return http.build();
    }

    // AuthenticationManager 빈 생성
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/img/**");
    }


}
