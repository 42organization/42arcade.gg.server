package io.pp.arcade.domain.security.oauth.v2.service;

import io.pp.arcade.domain.rank.service.RankNTService;
import io.pp.arcade.domain.security.oauth.v2.domain.ProviderType;
import io.pp.arcade.domain.security.oauth.v2.domain.UserPrincipal;
import io.pp.arcade.domain.security.oauth.v2.info.OAuthUserInfo;
import io.pp.arcade.domain.security.oauth.v2.info.OAuthUserInfoFactory;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.RacketType;
import io.pp.arcade.global.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RankNTService rankNTService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        ProviderType providerType = ProviderType.keyOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuthUserInfo userInfo = OAuthUserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        String intraId = userInfo.getIntraId();
        User savedUser = userRepository.findByIntraId(userInfo.getIntraId())
                .orElse(null);
        if (savedUser != null)
        {
            updateUser(savedUser , userInfo);
        } else {
            savedUser = createUser(userInfo, providerType);
            rankNTService.userToRedisRank(UserDto.from(savedUser));
        }

        return UserPrincipal.create(savedUser, user.getAttributes());
    }

    private User createUser(OAuthUserInfo userInfo, ProviderType providerType) {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .intraId(userInfo.getIntraId())
                .roleType(RoleType.USER)
                .imageUri(userInfo.getImageUrl())
                .statusMessage("")
                .racketType(RacketType.NONE)
                .ppp(1000)
                .eMail(userInfo.getEmail())
                .build();
        return userRepository.saveAndFlush(user);
    }

    private User updateUser(User user, OAuthUserInfo userInfo) {
        if (userInfo.getIntraId() != null && !user.getIntraId().equals(userInfo.getIntraId())) {
            user.setIntraId(userInfo.getIntraId());
        }
        return user;
    }
}
