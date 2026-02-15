package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.CompanyProfileMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlCompanyProfileRepository implements CompanyProfileRepository {

    private final JpaCompanyProfileRepository jpaRepository;
    private final CompanyProfileMapper mapper;

    @Override
    public void save(CompanyProfile profile) {
        log.debug("Saving company profile for ticker: {}", profile.getTicker());
        Optional<CompanyProfileEntity> existingEntity = jpaRepository.findByTicker(profile.getTicker());
        if (existingEntity.isPresent()) {
            CompanyProfileEntity entity = mapper.toEntity(profile);
            entity.setId(existingEntity.get().getId());
            jpaRepository.save(entity);
        } else {
            jpaRepository.save(mapper.toEntity(profile));
        }
        log.debug("Company profile saved successfully for ticker: {}", profile.getTicker());
    }

    @Override
    public Optional<CompanyProfile> findByTicker(String ticker) {
        log.debug("Finding company profile by ticker: {}", ticker);
        CompanyProfileEntity entity = jpaRepository.findAll().stream()
                .filter(p -> p.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);
        return entity != null ? Optional.of(mapper.toDomain(entity)) : Optional.empty();
    }

    @Override
    public void update(CompanyProfile profile) {
        log.debug("Updating company profile for ticker: {}", profile.getTicker());
        jpaRepository.save(mapper.toEntity(profile));
        log.debug("Company profile updated successfully for ticker: {}", profile.getTicker());
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        log.debug("Deleting company profile for ticker: {}", ticker);
        jpaRepository.deleteByTicker(ticker);
        log.debug("Company profile deleted successfully for ticker: {}", ticker);
    }

}
