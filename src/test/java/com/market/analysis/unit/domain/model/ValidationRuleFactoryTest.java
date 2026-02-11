package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ValidationRule;
import com.market.analysis.domain.model.ValidationRuleFactory;

@DisplayName("ValidationRuleFactory Tests")
class ValidationRuleFactoryTest {

    @Test
    @DisplayName("Should return rule when valid rule ID is provided")
    void shouldReturnRuleWhenValidRuleIdIsProvided() {
        Optional<ValidationRule> rule = ValidationRuleFactory.getRuleById("LOGO_PRESENT");
        
        assertTrue(rule.isPresent());
        assertEquals("LOGO_PRESENT", rule.get().getRuleId());
        assertEquals("Logo Present", rule.get().getRuleName());
    }

    @Test
    @DisplayName("Should return empty when invalid rule ID is provided")
    void shouldReturnEmptyWhenInvalidRuleIdIsProvided() {
        Optional<ValidationRule> rule = ValidationRuleFactory.getRuleById("INVALID_RULE");
        
        assertFalse(rule.isPresent());
    }

    @Test
    @DisplayName("Should return empty when null rule ID is provided")
    void shouldReturnEmptyWhenNullRuleIdIsProvided() {
        Optional<ValidationRule> rule = ValidationRuleFactory.getRuleById(null);
        
        assertFalse(rule.isPresent());
    }

    @Test
    @DisplayName("Should return all registered rules")
    void shouldReturnAllRegisteredRules() {
        List<ValidationRule> rules = ValidationRuleFactory.getAllRules();
        
        assertNotNull(rules);
        assertEquals(3, rules.size());
        
        // Verify all expected rules are present
        assertTrue(rules.stream().anyMatch(r -> r.getRuleId().equals("LOGO_PRESENT")));
        assertTrue(rules.stream().anyMatch(r -> r.getRuleId().equals("PRICE_ABOVE_SMA200")));
        assertTrue(rules.stream().anyMatch(r -> r.getRuleId().equals("VOLUME_ABOVE_AVERAGE")));
    }

    @Test
    @DisplayName("Should return true when rule exists")
    void shouldReturnTrueWhenRuleExists() {
        assertTrue(ValidationRuleFactory.ruleExists("LOGO_PRESENT"));
        assertTrue(ValidationRuleFactory.ruleExists("PRICE_ABOVE_SMA200"));
        assertTrue(ValidationRuleFactory.ruleExists("VOLUME_ABOVE_AVERAGE"));
    }

    @Test
    @DisplayName("Should return false when rule does not exist")
    void shouldReturnFalseWhenRuleDoesNotExist() {
        assertFalse(ValidationRuleFactory.ruleExists("INVALID_RULE"));
        assertFalse(ValidationRuleFactory.ruleExists(null));
    }

    @Test
    @DisplayName("Should return immutable list of rules")
    void shouldReturnImmutableListOfRules() {
        List<ValidationRule> rules = ValidationRuleFactory.getAllRules();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            rules.add(new ValidationRule() {
                @Override
                public String getRuleId() { return "TEST"; }
                @Override
                public String getRuleName() { return "Test"; }
                @Override
                public boolean evaluate(com.market.analysis.domain.model.Stock stock) { return false; }
                @Override
                public String getDescription() { return "Test"; }
            });
        });
    }
}
