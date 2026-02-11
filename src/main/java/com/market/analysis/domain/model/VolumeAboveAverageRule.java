package com.market.analysis.domain.model;

/**
 * Validation rule that checks if the stock's volume is greater than
 * its average volume.
 * This can indicate increased trading activity or interest.
 */
public class VolumeAboveAverageRule implements ValidationRule {

    private static final String RULE_ID = "VOLUME_ABOVE_AVERAGE";
    private static final String RULE_NAME = "Volume Above Average";
    private static final String DESCRIPTION = "Validates that current volume is above average volume";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }

    @Override
    public boolean evaluate(Stock stock) {
        if (stock == null) {
            return false;
        }

        Long volume = stock.getVolume();
        Long averageVolume = stock.getAverageVolume();

        if (volume == null || averageVolume == null || averageVolume == 0) {
            return false;
        }

        return volume > averageVolume;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
