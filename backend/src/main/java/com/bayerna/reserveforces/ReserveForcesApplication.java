package com.bayerna.reserveforces;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// 자체 JWT 인증(AuthService/JwtAuthenticationFilter)만 사용하므로 Spring Security의
// 기본 UserDetailsService 자동설정(임의 비밀번호 생성)은 사용하지 않는다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class ReserveForcesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReserveForcesApplication.class, args);
    }
}
