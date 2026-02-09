package com.market.analysis.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "earnings_data")
@Getter
@Setter
public class EarningsDataEntity {

    @Id
    private String ticker;

    private String date;

}
