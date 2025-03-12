package com.master.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_mst_tag")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Tag extends Auditable<String> {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.master.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String name;
    private String color;
}