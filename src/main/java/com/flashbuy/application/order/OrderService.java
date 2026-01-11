package com.flashbuy.application.order;

import com.flashbuy.common.BusinessException;
import com.flashbuy.domain.trade.entity.TradeOrder;
import com.flashbuy.domain.trade.repository.TradeOrderRepository;
import com.flashbuy.domain.item.repository.ProductSkuRepository;
import com.flashbuy.domain.item.entity.ProductSku;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service
 * Handles order queries and management
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final TradeOrderRepository orderRepository;
    private final ProductSkuRepository productSkuRepository;

    public OrderService(TradeOrderRepository orderRepository,
                        ProductSkuRepository productSkuRepository) {
        this.orderRepository = orderRepository;
        this.productSkuRepository = productSkuRepository;
    }

    /**
     * Get user order list
     *
     * @param userId user ID
     * @param status order status (optional, 0 for all)
     * @param pageNum page number
     * @param pageSize page size
     * @return order list
     */
    public OrderListResponse getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        log.info("Get user orders, userId={}, status={}", userId, status);

        List<TradeOrder> orders;
        if (status == null || status == 0) {
            orders = orderRepository.findByUserId(userId, pageNum, pageSize);
        } else {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageNum, pageSize);
        }

        int total = orderRepository.countByUserId(userId);

        List<OrderDetailResponse> orderDetails = orders.stream()
            .map(this::toOrderDetailResponse)
            .collect(Collectors.toList());

        return new OrderListResponse(
            total,
            orderDetails.size(),
            orderDetails
        );
    }

    /**
     * Get order detail
     *
     * @param userId user ID
     * @param orderId order ID
     * @return order detail
     */
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        log.info("Get order detail, userId={}, orderId={}", userId, orderId);

        TradeOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException(4004, "Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(4003, "No permission to access this order");
        }

        return toOrderDetailResponse(order);
    }

    /**
     * Get order count statistics
     *
     * @param userId user ID
     * @return order count
     */
    public OrderCountResponse getOrderCount(Long userId) {
        log.info("Get order count, userId={}", userId);

        int total = orderRepository.countByUserId(userId);
        int unpaid = orderRepository.countByUserIdAndStatus(userId, 0);
        int paid = orderRepository.countByUserIdAndStatus(userId, 1);
        int shipped = orderRepository.countByUserIdAndStatus(userId, 2);
        int completed = orderRepository.countByUserIdAndStatus(userId, 3);
        int cancelled = orderRepository.countByUserIdAndStatus(userId, 4);

        return new OrderCountResponse(total, unpaid, paid, shipped, completed, cancelled);
    }

    /**
     * Cancel order
     *
     * @param userId user ID
     * @param orderId order ID
     */
    public void cancelOrder(Long userId, Long orderId) {
        log.info("Cancel order, userId={}, orderId={}", userId, orderId);

        TradeOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException(4004, "Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(4003, "No permission to cancel this order");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(4005, "Only unpaid orders can be cancelled");
        }

        order.setStatus(4);
        orderRepository.update(order);
    }

    /**
     * Get order status
     *
     * @param userId user ID
     * @param orderId order ID
     * @return order status
     */
    public OrderStatusResponse getOrderStatus(Long userId, Long orderId) {
        log.info("Get order status, userId={}, orderId={}", userId, orderId);

        TradeOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException(4004, "Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(4003, "No permission to access this order");
        }

        String statusText = switch (order.getStatus()) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知状态";
        };

        return new OrderStatusResponse(
            order.getId(),
            order.getStatus(),
            statusText,
            order.getCreateTime(),
            order.getPayTime()
        );
    }

    private OrderDetailResponse toOrderDetailResponse(TradeOrder order) {
        String productName = "商品-" + order.getId();
        String productImage = "";
        BigDecimal productPrice = order.getPayAmount();

        String statusText = switch (order.getStatus()) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知状态";
        };

        return new OrderDetailResponse(
            order.getId(),
            order.getTotalAmount(),
            order.getPayAmount(),
            order.getStatus(),
            statusText,
            order.getOrderType(),
            productName,
            productImage,
            productPrice,
            order.getCreateTime(),
            order.getPayTime()
        );
    }

    // DTOs
    public record OrderListResponse(
        Integer total,
        Integer count,
        List<OrderDetailResponse> orders
    ) {}

    public record OrderDetailResponse(
        Long orderId,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        Integer status,
        String statusText,
        String orderType,
        String productName,
        String productImage,
        BigDecimal productPrice,
        LocalDateTime createTime,
        LocalDateTime payTime
    ) {}

    public record OrderCountResponse(
        Integer total,
        Integer unpaid,
        Integer paid,
        Integer shipped,
        Integer completed,
        Integer cancelled
    ) {}

    public record OrderStatusResponse(
        Long orderId,
        Integer status,
        String statusText,
        LocalDateTime createTime,
        LocalDateTime payTime
    ) {}
}
