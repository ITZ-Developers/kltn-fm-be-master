package com.master.repository;

import com.master.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findFirstByName(String name);
    Optional<Permission> findFirstByPermissionCode(String permissionCode);
    List<Permission> findAllByIdInAndKind(List<Long> id, Integer kind);
}
