package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
public interface JpaCompanyProfileRepository extends JpaRepository<CompanyProfileEntity, Long> {

    Optional<CompanyProfileEntity> findByTicker(String ticker);

    void deleteByTicker(String ticker);

}
