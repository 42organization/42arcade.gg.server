package io.pp.arcade.domain.pchange;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PChangeRepository extends JpaRepository<PChange, Integer> {
    Optional<List<PChange>> findAllByGame(Game game);
    Optional<List<PChange>> findAllByUserIntraId(String intraId);
    Page<PChange> findAllByUser(User user, Pageable pageable);
    Page<PChange> findAllByUserOrderByIdDesc(User user, Pageable pageable);
    /* status가 Entitiy에 없어요 ㅠㅠ */
    //     Page<PChange> findAllByUserAndStatusAndGameIdLessThanOrderByIdDesc(User user, String status, Integer gameId, Pageable pageable);
    Page<PChange> findAllByUserAndGameIdLessThanOrderByIdDesc(User user, Integer gameId, Pageable pageable);
    Optional<PChange> findByUserAndGame(User user, Game game);

    Page<PChange> findAllByOrderByIdDesc(Pageable pageable);
}
