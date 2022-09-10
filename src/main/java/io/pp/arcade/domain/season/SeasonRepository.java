package io.pp.arcade.domain.season;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.crypto.SealedObject;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository <Season, Integer> {
    Optional<Season> findSeasonByStartTimeIsBeforeAndEndTimeIsAfter(LocalDateTime startTime, LocalDateTime endTime);
    Optional<Season> findFirstByOrderByIdDesc();

    Page<Season> findAllByOrderByIdDesc(Pageable pageable);
}
