package com.master.repository;

import com.master.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findFirstByUsername(String username);
    Optional<Account> findFirstByEmail(String email);
    Optional<Account> findFirstByPhone(String phone);
    Boolean existsByEmail(String email);
    Boolean existsByGroupId(Long id);
    Optional<Account> findFirstByUsernameAndKind(String username, Integer kind);
    List<Account> findAllByGroupId(Long id);
}
