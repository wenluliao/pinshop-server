package com.flashbuy.application.seckill;

/**
 * 秒杀响应对象
 *
 * <p>包含秒杀订单的处理状态和结果信息</p>
 *
 * @param status 业务状态码（0:排队中 1:成功 -1:失败）
 * @param message 状态描述信息
 * @param orderId 订单ID（成功时返回，排队或失败时为null）
 * @param queueId 排队凭证ID（排队时返回，用于前端轮询查询）
 * @param timestamp 响应时间戳（毫秒）
 * @author FlashBuy Team
 * @since 1.0.0
 */
public record SeckillResponse(
        int status,
        String message,
        Long orderId,
        String queueId,
        Long timestamp
) {
    public static SeckillResponse queuing() {
        return new SeckillResponse(0, "Order processing, please wait", null, "Q-" + System.currentTimeMillis(), System.currentTimeMillis());
    }

    public static SeckillResponse success(Long orderId) {
        return new SeckillResponse(1, "Order created successfully", orderId, null, System.currentTimeMillis());
    }

    public static SeckillResponse failed(String message) {
        return new SeckillResponse(-1, message, null, null, System.currentTimeMillis());
    }
}
