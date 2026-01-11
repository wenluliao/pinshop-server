package com.flashbuy.api;

import com.flashbuy.application.admin.AdminDashboardService;
import com.flashbuy.application.admin.DashboardData;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Dashboard API Controller
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    public AdminController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * Get dashboard data
     * GET /api/v1/admin/dashboard
     */
    @GetMapping("/dashboard")
    public Result<DashboardData> getDashboard() {
        DashboardData data = adminDashboardService.getDashboard();
        return Result.ok(data);
    }
}
