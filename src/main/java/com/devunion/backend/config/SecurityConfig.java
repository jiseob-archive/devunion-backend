package com.devunion.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean //비밀번호 암호화에 사용할 passwordEncoder 빈 등록
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // 보안 필터 체인을 정의하여 API 접근 규칙을 설정
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 방어 비활성화 (API서버 이므로)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("api/auth/signup").permitAll() // 회원가입 API는 인증 없이 허용
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                );
        return http.build();
    }
}
