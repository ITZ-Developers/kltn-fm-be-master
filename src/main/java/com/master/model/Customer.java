package com.master.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_mst_customer")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Customer extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;
}