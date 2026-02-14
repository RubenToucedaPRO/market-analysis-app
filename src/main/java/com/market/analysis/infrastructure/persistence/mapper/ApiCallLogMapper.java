package com.market.analysis.infrastructure.persistence.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;

@Component
public class ApiCallLogMapper {

    public ApiCallLogEntity toEntity(String ticker, String timestamp) {
        if (ticker == null || timestamp == null) {
            return null;
        }
        return ApiCallLogEntity.builder()
                .ticker(ticker)
                .ocurredAt(Instant.parse(timestamp))
                .build();
    }

    public ApiCallLog toDomain(ApiCallLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return ApiCallLog.builder()
                .id(entity.getId())
                .ticker(entity.getTicker())
                .ocurredAt(entity.getOcurredAt())
                .build();
    }

}
