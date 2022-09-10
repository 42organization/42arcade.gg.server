package io.pp.arcade.domain.slot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Integer> {
    //List<Slot> findAllByCreatedAtAfter(LocalDateTime time);
    List<Slot> findAllByTimeAfterOrderByTimeAsc(LocalDateTime time);
    Optional<Slot> findByTime(LocalDateTime time);

    Page<Slot> findAllByOrderByIdDesc(Pageable pageable);
}
