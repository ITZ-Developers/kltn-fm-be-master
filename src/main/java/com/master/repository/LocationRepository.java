package com.master.repository;

import com.master.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Optional<Location> findFirstByTenantId(String tenantId);
    Optional<Location> findFirstByCustomerIdAndName(Long customerId, String name);
    boolean existsByCustomerId(Long customer);
    Optional<Location> findFirstByTenantIdAndCustomerStatusAndDbConfigIdIsNotNullAndStatusAndExpiredDateAfter(String tenantName, Integer customerStatus, Integer restaurantStatus, Date currentDate);
    @Query("SELECT DISTINCT l.tenantId FROM Location l")
    List<String> findAllDistinctTenantId();
}