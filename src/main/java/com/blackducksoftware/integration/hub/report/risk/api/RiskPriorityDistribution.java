package com.blackducksoftware.integration.hub.report.risk.api;

import java.util.Arrays;

public class RiskPriorityDistribution {

    private final int[] counts;

    public RiskPriorityDistribution() {
        counts = new int[RiskPriority.values().length];
    }

    /**
     * this constructor is needed only for internal purposes
     */
    private RiskPriorityDistribution(int[] counts) {
        if (counts != null) {
            this.counts = Arrays.copyOf(counts, counts.length);
        } else {
            this.counts = new int[RiskPriority.values().length];
        }
    }

    public RiskPriorityDistribution(
            int high,
            int medium,
            int low,
            int ok,
            int unknown) {
        counts = new int[RiskPriority.values().length];
        counts[RiskPriority.HIGH.ordinal()] = high;
        counts[RiskPriority.MEDIUM.ordinal()] = medium;
        counts[RiskPriority.LOW.ordinal()] = low;
        counts[RiskPriority.OK.ordinal()] = ok;
        counts[RiskPriority.UNKNOWN.ordinal()] = unknown;
    }

    public int[] getCounts() {
        return counts;
    }

    public int getCount(RiskPriority riskPriority) {
        return counts[riskPriority.ordinal()];
    }

    public RiskPriority getHighest() {
        for (RiskPriority riskPriority : RiskPriority.getRiskPrioritiesByDescendingRank()) {
            if (getCount(riskPriority) > 0) {
                return riskPriority;
            }
        }
        return RiskPriority.OK;
    }

    public int getHIGH() {
        return getCounts()[RiskPriority.HIGH.ordinal()];
    }

    public int getMEDIUM() {
        return getCounts()[RiskPriority.MEDIUM.ordinal()];
    }

    public int getLOW() {
        return getCounts()[RiskPriority.LOW.ordinal()];
    }

    public int getOK() {
        return getCounts()[RiskPriority.OK.ordinal()];
    }

    public int getUNKNOWN() {
        return getCounts()[RiskPriority.UNKNOWN.ordinal()];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(counts);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RiskPriorityDistribution other = (RiskPriorityDistribution) obj;
        if (!Arrays.equals(counts, other.counts)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RiskPriorityDistribution [counts=");
        builder.append(Arrays.toString(counts));
        builder.append("]");
        return builder.toString();
    }

}
