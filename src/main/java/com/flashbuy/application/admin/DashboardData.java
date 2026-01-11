package com.flashbuy.application.admin;

import java.math.BigDecimal;

/**
 * Dashboard Data DTO
 * Using Java Record for Native Image optimization
 */
public record DashboardData(
        BigDecimal todayGMV,
        Long newUserCount,
        Integer lowStockCount,
        Integer pendingShipmentCount,
        Long timestamp
) {
    public DashboardData {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}
