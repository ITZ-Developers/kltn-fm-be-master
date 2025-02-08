package com.master.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_mst_setting")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Setting extends Auditable<String>{
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.master.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String groupName;
    @Column(unique = true)
    private String keyName;
    @Column(columnDefinition = "text")
    private String valueData;
    private String dataType; // int, string, boolean, double, richtext
    private Boolean isSystem;
}
