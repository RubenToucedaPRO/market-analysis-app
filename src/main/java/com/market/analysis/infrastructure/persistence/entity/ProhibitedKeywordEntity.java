package com.market.analysis.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prohibited_keywords", uniqueConstraints = @UniqueConstraint(columnNames = "keyword"))
@Getter
@Setter
public class ProhibitedKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyword;

    @Column(nullable = false)
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
