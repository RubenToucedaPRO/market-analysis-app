package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;

@Repository
public interface JpaCompanyProfileRepository extends JpaRepository<CompanyProfileEntity, Long> {

    Optional<CompanyProfileEntity> findByTicker(String ticker);

    void deleteByTicker(String ticker);

}
