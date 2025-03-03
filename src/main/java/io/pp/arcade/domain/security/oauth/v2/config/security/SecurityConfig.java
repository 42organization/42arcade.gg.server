package io.pp.arcade.domain.security.oauth.v2.config.security;

import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.security.oauth.v2.config.properties.AppProperties;
import io.pp.arcade.domain.security.oauth.v2.config.properties.CorsProperties;
import io.pp.arcade.domain.security.oauth.v2.exception.RestAuthenticationEntryPoint;
import io.pp.arcade.domain.security.oauth.v2.filter.TokenAuthenticationFilter;
import io.pp.arcade.domain.security.oauth.v2.handler.OAuthAuthenticationFailureHandler;
import io.pp.arcade.domain.security.oauth.v2.handler.OAuthAuthenticationSuccessHandler;
import io.pp.arcade.domain.security.oauth.v2.handler.TokenAccessDeniedHandler;
import io.pp.arcade.domain.security.oauth.v2.repository.OAuthAuthorizationRequestBasedOnCookieRepository;
import io.pp.arcade.domain.security.oauth.v2.repository.UserRefreshTokenRepository;
import io.pp.arcade.domain.security.oauth.v2.service.CustomOAuth2UserService;
import io.pp.arcade.domain.security.oauth.v2.service.CustomUserDetailsService;
import io.pp.arcade.domain.security.oauth.v2.token.AuthTokenProvider;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.util.ApplicationYmlRead;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final CorsProperties corsProperties;
    private final AppProperties appProperties;
    private final AuthTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenAccessDeniedHandler tokenAccessDeniedHandler;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final ApplicationYmlRead applicationYmlRead;
    private final RankRedisService rankRedisService;
    /*
     * UserDetailsService 설정
     * */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .csrf().disable()
                    .formLogin().disable()
                    .httpBasic().disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                    .accessDeniedHandler(tokenAccessDeniedHandler)
                .and()
                    .oauth2Login()
                    .authorizationEndpoint()
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                .and()
                    .redirectionEndpoint()
                    .baseUri("/*/oauth2/code/*")
                .and()
                    .userInfoEndpoint()
                    .userService(oAuth2UserService)
                .and()
                    .successHandler(oAuth2AuthenticationSuccessHandler())
                    .failureHandler(oAuth2AuthenticationFailureHandler());

        //http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /*
     * auth 매니저 설정
     * */
    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

//    @Bean
//    public TokenAuthenticationFilter tokenAuthenticationFilter() {
//        return new TokenAuthenticationFilter(tokenProvider);
//    }

    /*
     * security 설정 시, 사용할 인코더 설정
     * */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * 쿠키 기반 인가 Repository
     * 인가 응답을 연계 하고 검증할 때 사용.
     * */
    @Bean
    public OAuthAuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuthAuthorizationRequestBasedOnCookieRepository();
    }

    /*
     * Oauth 인증 성공 핸들러
     * */
    @Bean
    public OAuthAuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuthAuthenticationSuccessHandler(
                rankRedisService,
                tokenProvider,
                userRepository,
                appProperties,
                userRefreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                applicationYmlRead
        );
    }

    /*
     * Oauth 인증 실패 핸들러
     * */
    @Bean
    public OAuthAuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuthAuthenticationFailureHandler(oAuth2AuthorizationRequestBasedOnCookieRepository(), applicationYmlRead);
    }
    /*
     * Cors 설정
     * */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        corsConfig.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        corsConfig.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins().split(",")));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(corsConfig.getMaxAge());

        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }
}