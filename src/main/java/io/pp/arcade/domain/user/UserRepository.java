package io.pp.arcade.domain.user;

import io.pp.arcade.global.type.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByIntraId(String intraId);
    List<User> findByIntraIdContains(String intraId);
    @Query(nativeQuery = false, value = "select u from User as u where u.intraId like %:partial%")
    Page<User> findByIntraIdContains(@Param("partial") String partial, Pageable pageable);
    List<User> findAllByRoleType(RoleType roleType);
    User getUserByIntraId(String IntraId);
}
