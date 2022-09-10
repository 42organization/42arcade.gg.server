package io.pp.arcade.domain.rank;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RankRedisRepository extends CrudRepository<RankRedis, String> {
    Optional<RankRedis> findByIntraIdAndGameType(String intraId, String gameType);
    List<RankRedis> findByIntraIdAndGameType(String intraId);
}
