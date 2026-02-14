package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
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
        try {
            Optional<CompanyProfileEntity> existingEntity = jpaRepository.findByTicker(profile.getTicker());
            if (existingEntity.isPresent()) {
                CompanyProfileEntity entity = mapper.toEntity(profile);
                entity.setId(existingEntity.get().getId());
                jpaRepository.save(entity);
            } else {
                jpaRepository.save(mapper.toEntity(profile));
            }
        } catch (DataAccessException e) {
            log.error("Database error saving company profile for ticker: {}", profile.getTicker(), e);
            throw new PersistenceException("Error saving company profile for ticker: " + profile.getTicker(), e);
        }
    }

    @Override
    public Optional<CompanyProfile> findByTicker(String ticker) {
        try {
            CompanyProfileEntity entity = jpaRepository.findAll().stream()
                    .filter(p -> p.getTicker().equalsIgnoreCase(ticker))
                    .findFirst()
                    .orElse(null);
            return entity != null ? Optional.of(mapper.toDomain(entity)) : Optional.empty();
        } catch (DataAccessException e) {
            log.error("Database error finding company profile for ticker: {}", ticker, e);
            throw new PersistenceException("Error finding company profile for ticker: " + ticker, e);
        }
    }

    @Override
    public void update(CompanyProfile profile) {
        try {
            jpaRepository.save(mapper.toEntity(profile));
        } catch (DataAccessException e) {
            log.error("Database error updating company profile for ticker: {}", profile.getTicker(), e);
            throw new PersistenceException("Error updating company profile for ticker: " + profile.getTicker(), e);
        }
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        try {
            jpaRepository.deleteByTicker(ticker);
        } catch (DataAccessException e) {
            log.error("Database error deleting company profile for ticker: {}", ticker, e);
            throw new PersistenceException("Error deleting company profile for ticker: " + ticker, e);
        }
    }

}
