package com.master.repository;

import com.master.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findFirstByNameAndKind(String name, Integer kind);
    Optional<Permission> findFirstByPermissionCodeAndKind(String permissionCode, Integer kind);
    List<Permission> findAllByIdInAndKind(List<Long> id, Integer kind);
    @Query("SELECT p.permissionCode FROM Group g JOIN g.permissions p WHERE g.kind = :kind AND p.id in :ids")
    List<String> findPermissionCodesByGroupKindAndIdIn(@Param("kind") Integer kind, @Param("ids") List<Long> ids);
}
