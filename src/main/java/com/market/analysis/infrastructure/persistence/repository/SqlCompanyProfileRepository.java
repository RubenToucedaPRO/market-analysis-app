package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.CompanyProfileMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlCompanyProfileRepository implements CompanyProfileRepository {

    private final JpaCompanyProfileRepository jpaRepository;
    private final CompanyProfileMapper mapper;

    @Override
    public void save(CompanyProfile profile) {
        Optional<CompanyProfileEntity> existingEntity = jpaRepository.findByTicker(profile.getTicker());
        if (existingEntity.isPresent()) {
            CompanyProfileEntity entity = mapper.toEntity(profile);
            entity.setId(existingEntity.get().getId());
            jpaRepository.save(entity);
        } else {
            jpaRepository.save(mapper.toEntity(profile));
        }
    }

    @Override
    public Optional<CompanyProfile> findByTicker(String ticker) {
        CompanyProfileEntity entity = jpaRepository.findAll().stream()
                .filter(p -> p.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);
        return entity != null ? Optional.of(mapper.toDomain(entity)) : Optional.empty();
    }

    @Override
    public void update(CompanyProfile profile) {
        jpaRepository.save(mapper.toEntity(profile));
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        jpaRepository.deleteByTicker(ticker);
    }

}
