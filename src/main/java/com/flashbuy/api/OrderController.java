package com.flashbuy.api;

import com.flashbuy.application.order.OrderService;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

/**
 * Order Controller
 * Provides order query and management APIs
 */
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Get user order list
     *
     * @param userId user ID
     * @param status order status (optional, 0 for all)
     * @param pageNum page number (default 1)
     * @param pageSize page size (default 10)
     * @return order list
     */
    @GetMapping("/list")
    public Result<OrderService.OrderListResponse> getOrderList(
        @RequestParam Long userId,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.getUserOrders(userId, status, pageNum, pageSize));
    }

    /**
     * Get order detail
     *
     * @param userId user ID
     * @param orderId order ID
     * @return order detail
     */
    @GetMapping("/{orderId}")
    public Result<OrderService.OrderDetailResponse> getOrderDetail(
        @RequestParam Long userId,
        @PathVariable Long orderId) {
        return Result.ok(orderService.getOrderDetail(userId, orderId));
    }

    /**
     * Get order count statistics
     *
     * @param userId user ID
     * @return order count
     */
    @GetMapping("/count")
    public Result<OrderService.OrderCountResponse> getOrderCount(@RequestParam Long userId) {
        return Result.ok(orderService.getOrderCount(userId));
    }

    /**
     * Cancel order
     *
     * @param userId user ID
     * @param orderId order ID
     * @return void
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
        @RequestParam Long userId,
        @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.ok();
    }

    /**
     * Get order status
     *
     * @param userId user ID
     * @param orderId order ID
     * @return order status
     */
    @GetMapping("/{orderId}/status")
    public Result<OrderService.OrderStatusResponse> getOrderStatus(
        @RequestParam Long userId,
        @PathVariable Long orderId) {
        return Result.ok(orderService.getOrderStatus(userId, orderId));
    }
}
