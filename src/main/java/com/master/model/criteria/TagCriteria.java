package com.master.model.criteria;

import com.master.constant.MasterConstant;
import com.master.model.Tag;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TagCriteria {
    private Long id;
    private String name;
    private Integer status;
    private Integer isPaged = MasterConstant.BOOLEAN_TRUE;

    public Specification<Tag> getCriteria() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getId() != null) {
                predicates.add(cb.equal(root.get("id"), getId()));
            }
            if (StringUtils.isNotBlank(getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
            }
            if (getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}