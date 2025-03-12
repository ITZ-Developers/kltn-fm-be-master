package com.master.repository;

import com.master.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Optional<Location> findFirstByTenantId(String tenantId);
    Optional<Location> findFirstByCustomerIdAndName(Long customerId, String name);
    boolean existsByCustomerId(Long customer);
    @Modifying
    @Transactional
    @Query("UPDATE Location l SET l.tag.id = NULL WHERE l.tag.id = :id")
    void updateAllTagIdToNullByTagId(@Param("id") Long id);
    List<Location> findAllByCustomerIdAndCustomerStatusAndDbConfigIdIsNotNullAndStatusAndExpiredDateAfter(Long customerId, Integer customerStatus, Integer restaurantStatus, Date currentDate);
}