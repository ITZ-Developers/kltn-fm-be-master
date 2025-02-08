package com.master.model.criteria;

import com.master.constant.MasterConstant;
import com.master.model.Customer;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerCriteria implements Serializable {
    private Long id;
    private String name;
    private Long accountId;
    private Integer status;
    private Integer sortDate;
    private Integer isPaged = MasterConstant.IS_PAGED_TRUE;

    public Specification<Customer> getCriteria() {
        return new Specification<Customer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (StringUtils.isNotBlank(getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if (getAccountId() != null) {
                    predicates.add(cb.equal(root.get("account").get("id"), getAccountId()));
                }
                if (getSortDate() != null) {
                    if (getSortDate().equals(MasterConstant.SORT_DATE_ASC)) {
                        query.orderBy(cb.asc(root.get("createdDate")));
                    } else {
                        query.orderBy(cb.desc(root.get("createdDate")));
                    }
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
