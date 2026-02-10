package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.CompanyProfileMapper;

import jakarta.transaction.Transactional;
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
    public CompanyProfile findByTicker(String ticker) {
        CompanyProfileEntity entity = jpaRepository.findAll().stream()
                .filter(p -> p.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);
        return entity != null ? mapper.toModel(entity) : null;
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
