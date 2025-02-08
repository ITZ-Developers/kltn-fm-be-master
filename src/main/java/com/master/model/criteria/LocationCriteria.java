package com.master.model.criteria;

import com.master.constant.MasterConstant;
import com.master.model.Customer;
import com.master.model.DbConfig;
import com.master.model.Location;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class LocationCriteria implements Serializable {
    private Long id;
    private String name;
    private String tenantId;
    private String address;
    private String hotline;
    private String language;
    private Integer status;
    private Integer sortDate;
    private Long customerId;
    private Integer isPaged = MasterConstant.IS_PAGED_TRUE;
    private Integer ignoreDbConfig = MasterConstant.IGNORE_ENTITY_RELATIONSHIP_FALSE;

    public Specification<Location> getCriteria() {
        return new Specification<Location>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Location> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
                if (StringUtils.isNotBlank(getTenantId())) {
                    predicates.add(cb.like(cb.lower(root.get("tenantId")), "%" + getTenantId().toLowerCase() + "%"));
                }
                if (StringUtils.isNotBlank(getAddress())) {
                    predicates.add(cb.like(cb.lower(root.get("address")), "%" + getAddress().toLowerCase() + "%"));
                }
                if (StringUtils.isNotBlank(getHotline())) {
                    predicates.add(cb.like(root.get("hotline"), "%" + getHotline() + "%"));
                }
                if (StringUtils.isNotBlank(getLanguage())) {
                    predicates.add(cb.equal(root.get("language"), getLanguage()));
                }
                if (getCustomerId() != null) {
                    Join<Location, Customer> joinCustomer = root.join("customer", JoinType.INNER);
                    predicates.add(cb.equal(joinCustomer.get("id"), getCustomerId()));
                }
                if (MasterConstant.IGNORE_ENTITY_RELATIONSHIP_TRUE.equals(getIgnoreDbConfig())) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<DbConfig> subRoot = subquery.from(DbConfig.class);
                    subquery.select(subRoot.get("location").get("id"));
                    predicates.add(cb.not(root.get("id").in(subquery)));
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
