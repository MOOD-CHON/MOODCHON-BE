package com.example.moodchon.global.config;

import com.example.moodchon.auth.CookieAuthorizationRequestRepository;
import com.example.moodchon.auth.CustomOAuth2UserService;
import com.example.moodchon.auth.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/oauth2/**",
            "/login/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository,
                           CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                           CustomOAuth2UserService customOAuth2UserService) {
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }
}