package io.pp.arcade.domain.team;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Page<Team> findAllByOrderByIdDesc(Pageable pageable);
}
