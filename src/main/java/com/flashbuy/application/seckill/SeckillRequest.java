package com.flashbuy.application.seckill;

/**
 * 秒杀请求对象
 *
 * <p>包含执行秒杀所需的所有参数</p>
 *
 * @param eventId 秒杀场次ID（必填）
 * @param skuId 商品SKU编号（必填）
 * @param count 购买数量（可选，默认1，单次限购1件）
 * @param userId 用户ID（必填，从登录Token中获取）
 * @author FlashBuy Team
 * @since 1.0.0
 */
public record SeckillRequest(
        Long eventId,
        Long skuId,
        Integer count,
        Long userId
) {
    public SeckillRequest {
        if (count == null || count <= 0) {
            count = 1;
        }
    }
}
