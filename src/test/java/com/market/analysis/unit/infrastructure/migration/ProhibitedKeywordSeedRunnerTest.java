package com.market.analysis.unit.infrastructure.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;
import com.market.analysis.infrastructure.migration.ProhibitedKeywordSeedRunner;

@DisplayName("ProhibitedKeywordSeedRunner Unit Tests")
@ExtendWith(MockitoExtension.class)
class ProhibitedKeywordSeedRunnerTest {

    @Mock
    private ProhibitedKeywordRepository prohibitedKeywordRepository;

    @InjectMocks
    private ProhibitedKeywordSeedRunner runner;

    @Test
    @DisplayName("Should seed default keywords when repository is empty")
    void shouldSeedDefaultKeywordsWhenRepositoryIsEmpty() throws Exception {
        when(prohibitedKeywordRepository.findAll()).thenReturn(List.of());

        runner.run();

        ArgumentCaptor<ProhibitedKeyword> captor = ArgumentCaptor.forClass(ProhibitedKeyword.class);
        verify(prohibitedKeywordRepository, times(16)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ProhibitedKeyword::getKeyword)
                .containsExactly(
                        "ACQUISITION", "MERGER", "ETF", "FUND", "TRUST",
                        "BULL", "BEAR", "2X", "3X",
                        "THERAPEUTICS", "PHARMA", "BIO", "ONCOLOGY",
                        "LP", "PARTNERS", "WARRANTS");
        assertThat(captor.getAllValues()).allMatch(ProhibitedKeyword::isActive);
    }

    @Test
    @DisplayName("Should skip seed when repository already has keywords")
    void shouldSkipSeedWhenRepositoryAlreadyHasKeywords() throws Exception {
        when(prohibitedKeywordRepository.findAll()).thenReturn(List.of(
                ProhibitedKeyword.builder().keyword("ETF").active(true).build()));

        runner.run();

        verify(prohibitedKeywordRepository, never()).save(any());
    }
}
