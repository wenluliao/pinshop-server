package com.flashbuy.api;

import com.flashbuy.application.payment.PaymentService;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

/**
 * Payment Controller
 * Provides payment creation and callback processing APIs
 */
@RestController
@RequestMapping("/api/v1/pay")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create payment
     *
     * @param request create payment request
     * @return payment info with pay URL
     */
    @PostMapping("/create")
    public Result<PaymentService.CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        return Result.ok(
            paymentService.createPayment(request.userId(), request.orderId(), request.payType())
        );
    }

    /**
     * WeChat payment callback
     *
     * @param request callback request
     * @return callback response
     */
    @PostMapping("/callback/wechat")
    public PaymentService.PaymentCallbackResponse wechatCallback(@RequestBody PaymentService.PaymentCallbackRequest request) {
        return paymentService.processCallback("wechat", request);
    }

    /**
     * Alipay payment callback
     *
     * @param request callback request
     * @return callback response
     */
    @PostMapping("/callback/alipay")
    public PaymentService.PaymentCallbackResponse alipayCallback(@RequestBody PaymentService.PaymentCallbackRequest request) {
        return paymentService.processCallback("alipay", request);
    }

    /**
     * Get payment status
     *
     * @param userId user ID
     * @param orderId order ID
     * @return payment status
     */
    @GetMapping("/status")
    public Result<PaymentService.PaymentStatusResponse> getPaymentStatus(
        @RequestParam Long userId,
        @RequestParam Long orderId) {
        return Result.ok(paymentService.getPaymentStatus(userId, orderId));
    }

    // Request DTOs
    public record CreatePaymentRequest(
        Long userId,
        Long orderId,
        String payType
    ) {}
}
