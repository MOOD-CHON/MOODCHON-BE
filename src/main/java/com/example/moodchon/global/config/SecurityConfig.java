package com.example.moodchon.global.config;

import com.example.moodchon.auth.CookieAuthorizationRequestRepository;
import com.example.moodchon.auth.CustomOAuth2UserService;
import com.example.moodchon.auth.CustomOidcUserService;
import com.example.moodchon.auth.handler.CustomAuthenticationEntryPoint;
import com.example.moodchon.auth.handler.OAuth2SuccessHandler;
import com.example.moodchon.auth.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/oauth2/**",
            "/login/**",
            "/api/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

    public SecurityConfig(CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository,
                           CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                           CustomOAuth2UserService customOAuth2UserService,
                           CustomOidcUserService customOidcUserService,
                           OAuth2SuccessHandler oAuth2SuccessHandler,
                           JwtAuthenticationFilter jwtAuthenticationFilter,
                           OAuth2AuthorizationRequestResolver authorizationRequestResolver) {
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authorizationRequestResolver = authorizationRequestResolver;
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
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                                .authorizationRequestResolver(authorizationRequestResolver))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService))
                        .successHandler(oAuth2SuccessHandler))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}