package com.master.repository;

import com.master.model.DbConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface DbConfigRepository extends JpaRepository<DbConfig, Long>, JpaSpecificationExecutor<DbConfig> {
    Optional<DbConfig> findFirstByNameAndInitialize(String name, Boolean initialize);
    Optional<DbConfig> findFirstByUsername(String username);
    Optional<DbConfig> findFirstByUrl(String url);
    boolean existsByServerProviderId(Long serverProviderId);
    Optional<DbConfig> findFirstByLocationId(Long locationId);
    @Transactional
    void deleteAllByLocationId(Long locationId);
    @Query("SELECT d FROM DbConfig d JOIN Location l ON d.location.id = l.id WHERE d.initialize = TRUE AND l.tenantId = :tenantId")
    Optional<DbConfig> findByTenantId(@Param("tenantId") String tenantId);
    Integer countByServerProviderId(Long serverProviderId);
}