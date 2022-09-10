package io.pp.arcade.domain.game;

import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findBySlot(Slot slot);
    Optional<Game> findBySlotId(Integer slotId);
    Page<Game> findAllByTypeOrderByIdDesc(Pageable pageable, GameType type);
    Page<Game> findByStatus(String status, Pageable pageable);
    Page<Game> findByIdLessThanAndStatusOrderByTimeDesc(Integer id, StatusType status, Pageable pageable);
    Page<Game> findByIdLessThanOrderByTimeDesc (Integer id, Pageable pageable);
}
