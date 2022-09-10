package io.pp.arcade.domain.noti;

import io.pp.arcade.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotiRepository extends JpaRepository<Noti, Integer> {
    List<Noti> findAllByUser(User user);
    List<Noti> findAllByUserOrderByIdDesc(User user);
    Optional<Noti> findByIdAndUserId(Integer Id, Integer userId);
    Integer countAllNByUserAndIsChecked(User user, Boolean isChecked);
    void deleteAllByUser(User user);
    void deleteById(Integer id);
    Page<Noti> findAllByOrderByIdDesc(Pageable pageable);
}
