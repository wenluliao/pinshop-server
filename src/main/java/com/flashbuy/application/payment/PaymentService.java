package com.flashbuy.application.payment;

import com.flashbuy.common.BusinessException;
import com.flashbuy.domain.trade.entity.TradeOrder;
import com.flashbuy.domain.trade.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Service
 * Handles payment creation, callback processing, and status queries
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final TradeOrderRepository orderRepository;

    public PaymentService(TradeOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Create payment
     *
     * @param userId user ID
     * @param orderId order ID
     * @param payType payment type (wechat, alipay)
     * @return payment info
     */
    public CreatePaymentResponse createPayment(Long userId, Long orderId, String payType) {
        log.info("Create payment, userId={}, orderId={}, payType={}", userId, orderId, payType);

        TradeOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new BusinessException(4004, "Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(4003, "No permission to access this order");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(4006, "Order already paid or cancelled");
        }

        // Generate payment ID
        String paymentId = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        // In real implementation, call WeChat/Alipay SDK here
        // For now, return mock payment data
        return new CreatePaymentResponse(
            paymentId,
            orderId,
            order.getPayAmount(),
            payType,
            generateMockPayUrl(payType, paymentId, order.getPayAmount())
        );
    }

    /**
     * Process payment callback
     *
     * @param payType payment type
     * @param request callback request
     * @return callback response
     */
    @Transactional
    public PaymentCallbackResponse processCallback(String payType, PaymentCallbackRequest request) {
        log.info("Process payment callback, payType={}, paymentId={}", payType, request.paymentId());

        // In real implementation, verify signature first
        // For now, just process payment

        // Extract order ID from payment ID (mock implementation)
        // In real scenario, you'd have a separate payment table to track payment_id -> order_id mapping
        TradeOrder order = findOrderByPaymentId(request.paymentId());
        if (order == null) {
            log.error("Order not found for paymentId={}", request.paymentId());
            return new PaymentCallbackResponse("FAIL", "Order not found");
        }

        if (order.getStatus() != 0) {
            log.warn("Order {} already processed, status={}", order.getId(), order.getStatus());
            return new PaymentCallbackResponse("SUCCESS", "OK");
        }

        // Update order status
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderRepository.update(order);

        log.info("Order {} paid successfully", order.getId());
        return new PaymentCallbackResponse("SUCCESS", "OK");
    }

    /**
     * Query payment status
     *
     * @param userId user ID
     * @param orderId order ID
     * @return payment status
     */
    public PaymentStatusResponse getPaymentStatus(Long userId, Long orderId) {
        log.info("Get payment status, userId={}, orderId={}", userId, orderId);

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
            case 4 -> "已取消";
            default -> "未知状态";
        };

        return new PaymentStatusResponse(
            order.getId(),
            order.getStatus(),
            statusText,
            order.getPayAmount(),
            order.getPayTime()
        );
    }

    private TradeOrder findOrderByPaymentId(String paymentId) {
        // Mock implementation - in production, you'd have a payment table
        // For now, return null to indicate this needs proper implementation
        return null;
    }

    private String generateMockPayUrl(String payType, String paymentId, java.math.BigDecimal amount) {
        // Mock pay URL - in production, call actual payment SDK
        if ("wechat".equalsIgnoreCase(payType)) {
            return "weixin://wxpay/bizpayurl?pr=" + paymentId;
        } else if ("alipay".equalsIgnoreCase(payType)) {
            return "alipays://platformapi/startapp?paymentId=" + paymentId;
        }
        return "https://pay.example.com/pay?paymentId=" + paymentId;
    }

    // DTOs
    public record CreatePaymentResponse(
        String paymentId,
        Long orderId,
        java.math.BigDecimal amount,
        String payType,
        String payUrl
    ) {}

    public record PaymentCallbackRequest(
        String paymentId,
        String transactionId,
        java.math.BigDecimal amount,
        String status
    ) {}

    public record PaymentCallbackResponse(
        String returnCode,
        String returnMsg
    ) {}

    public record PaymentStatusResponse(
        Long orderId,
        Integer status,
        String statusText,
        java.math.BigDecimal payAmount,
        LocalDateTime payTime
    ) {}
}
