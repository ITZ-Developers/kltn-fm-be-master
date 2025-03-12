package com.master.repository;

import com.master.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
    Boolean existsByColor(String color);
    Boolean existsByColorAndIdNot(String color, Long id);
}