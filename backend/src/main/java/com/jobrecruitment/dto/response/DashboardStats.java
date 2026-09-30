package com.jobrecruitment.dto.response;

import java.util.Map;

public class DashboardStats {
    private Map<String, Object> stats;

    public DashboardStats() {}

    public DashboardStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }
}
