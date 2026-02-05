package com.market.analysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a rule definition in the database.
 * Corresponds to the domain model RuleDefinition.
 */
@Entity
@Table(name = "rule_definitions")
@Getter
@Setter
public class RuleDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "requires_param", nullable = false)
    private boolean requiresParam;

    @Column(length = 1000)
    private String description;
}
