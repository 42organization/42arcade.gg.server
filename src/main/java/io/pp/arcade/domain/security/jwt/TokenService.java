package io.pp.arcade.domain.security.jwt;

import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.RoleType;
import io.pp.arcade.global.exception.AccessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TokenService {
    private final TokenRepository repository;

    public UserDto findUserByAccessToken(String accessToken){
        User user = repository.findByAccessToken(accessToken)
                .orElseThrow(() -> new AccessException("{front.url}"))
                .getUser();
        return UserDto.from(user);
    }

    public UserDto findAdminByAccessToken(String accessToken){
        User user = repository.findByAccessToken(accessToken)
                .orElseThrow(() -> new AccessException("{front.url}"))
                .getUser();
        if (user.getRoleType() != RoleType.ADMIN)
            throw new AccessException("{front.url}");
        return UserDto.from(user);
    }

    public Token findByAccessToken(String accessToken){
        return repository.findByAccessToken(accessToken)
                .orElseThrow(() -> new BusinessException("{token.notfound}"));
    }

    public UserDto findAdminByRefreshToken(String refreshToken) {
        User user = repository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AccessException("{front.url}"))
                .getUser();
        if (user.getRoleType() != RoleType.ADMIN)
            throw new AccessException("{front.url}");
        return UserDto.from(user);
    }
}
