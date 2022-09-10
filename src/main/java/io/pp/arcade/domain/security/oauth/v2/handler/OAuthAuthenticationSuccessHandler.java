package io.pp.arcade.domain.security.oauth.v2.handler;

import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.security.jwt.Token;
import io.pp.arcade.domain.security.oauth.v2.config.properties.AppProperties;
import io.pp.arcade.domain.security.oauth.v2.domain.ProviderType;
import io.pp.arcade.domain.security.oauth.v2.info.OAuthUserInfo;
import io.pp.arcade.domain.security.oauth.v2.info.OAuthUserInfoFactory;
import io.pp.arcade.domain.security.oauth.v2.repository.OAuthAuthorizationRequestBasedOnCookieRepository;
import io.pp.arcade.domain.security.oauth.v2.repository.UserRefreshTokenRepository;
import io.pp.arcade.domain.security.oauth.v2.token.AuthToken;
import io.pp.arcade.domain.security.oauth.v2.token.AuthTokenProvider;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.type.RoleType;
import io.pp.arcade.global.util.ApplicationYmlRead;
import io.pp.arcade.global.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Component
@RequiredArgsConstructor
public class OAuthAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private final String REFRESH_TOKEN = "refresh_token";
    private final RankRedisService rankRedisService;
    private final AuthTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final OAuthAuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final ApplicationYmlRead applicationYmlRead;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        Map<String, String> tokens = getTokens(request, response, authentication);
//        String targetUrl = applicationYmlRead.getFrontUrl();
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }
        clearAuthenticationAttributes(request, response);
//        response.addHeader("access_token", tokens.get("accessToken"));
//        request.getRequestDispatcher(targetUrl).forward(request, response);
//        response.sendRedirect(targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.keyOf (authToken.getAuthorizedClientRegistrationId().toUpperCase());

        OidcUser user = ((OidcUser) authentication.getPrincipal());
        OAuthUserInfo userInfo = OAuthUserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        Collection<? extends GrantedAuthority> authorities = ((OidcUser) authentication.getPrincipal()).getAuthorities();

        RoleType roleType = hasAuthority(authorities, RoleType.ADMIN.getKey()) ? RoleType.ADMIN : RoleType.USER;

        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userInfo.getIntraId(),
                roleType.getKey(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        // refresh 토큰 설정
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );

        // DB 저장
        User saveUser = userRepository.findByIntraId(userInfo.getIntraId()).orElseThrow();

        Token userRefreshToken = userRefreshTokenRepository.findByUser(saveUser);
        if (userRefreshToken != null) {
            userRefreshToken.setRefreshToken(refreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
        } else {
            userRefreshToken = new Token(saveUser, refreshToken.getToken(), accessToken.getToken());
            userRefreshTokenRepository.saveAndFlush(userRefreshToken);
        }


        int cookieMaxAge = (int) refreshTokenExpiry / 60;

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge);

        return UriComponentsBuilder.fromUriString(applicationYmlRead.getFrontUrl())
//                return UriComponentsBuilder.fromUriString("https://42byte.kr")
                .queryParam("token", userRefreshToken == null ? accessToken.getToken() : userRefreshToken.getAccessToken())
                .build().toUriString();
    }

    private Map<String, String> getTokens(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.keyOf (authToken.getAuthorizedClientRegistrationId().toUpperCase());

        OidcUser user = ((OidcUser) authentication.getPrincipal());
        OAuthUserInfo userInfo = OAuthUserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        Collection<? extends GrantedAuthority> authorities = ((OidcUser) authentication.getPrincipal()).getAuthorities();

        RoleType roleType = hasAuthority(authorities, RoleType.ADMIN.getKey()) ? RoleType.ADMIN : RoleType.USER;

        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userInfo.getIntraId(),
                roleType.getKey(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        // refresh 토큰 설정
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );

        // DB 저장
        User saveUser = userRepository.findByIntraId(userInfo.getIntraId()).orElseThrow();

        Token userRefreshToken = userRefreshTokenRepository.findByUser(saveUser);
        if (userRefreshToken != null) {
            userRefreshToken.setRefreshToken(refreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
        } else {
            userRefreshToken = new Token(saveUser, refreshToken.getToken(), accessToken.getToken());
            userRefreshTokenRepository.saveAndFlush(userRefreshToken);
        }


        int cookieMaxAge = (int) refreshTokenExpiry / 60;

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken.getToken());
        tokens.put("refreshToken", refreshToken.getToken());
        return tokens;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        //authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        if (authorities == null) {
            return false;
        }

        for (GrantedAuthority grantedAuthority : authorities) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }
}

