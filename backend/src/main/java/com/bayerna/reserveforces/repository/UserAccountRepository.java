package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.user.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
