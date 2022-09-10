package io.pp.arcade.domain.rank.service;

import io.pp.arcade.domain.user.dto.UserDto;

public interface RankNTService {
    void userToRedisRank(UserDto user);
}
