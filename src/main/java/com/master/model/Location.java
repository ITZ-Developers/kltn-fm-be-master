package com.master.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_mst_location")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Location extends Auditable<String> {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.master.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String tenantId;
    private String name;
    private String address;
    private String logoPath;
    private String bannerPath;
    private String hotline;
    @Column(columnDefinition = "TEXT")
    private String settings;
    private String language;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
