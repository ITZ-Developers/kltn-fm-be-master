package com.master.repository;

import com.master.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Optional<Location> findFirstByTenantId(String tenantId);
    Optional<Location> findFirstByCustomerIdAndName(Long customerId, String name);
    boolean existsByCustomerId(Long customer);
}