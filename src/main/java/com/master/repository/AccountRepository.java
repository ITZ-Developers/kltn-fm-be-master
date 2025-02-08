package com.master.repository;

import com.master.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findFirstByUsername(String username);
    Optional<Account> findFirstByEmail(String email);
    Optional<Account> findFirstByPhone(String phone);
    boolean existsByGroupId(Long groupId);
}
