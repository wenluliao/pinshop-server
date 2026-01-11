package com.flashbuy.application.groupbuy;

/**
 * 参与拼团请求
 *
 * @param sessionId 拼团会话ID
 * @param userId 用户ID
 * @param skuId 商品SKU ID
 * @param addressId 收货地址ID
 */
public record JoinGroupRequest(
        Long sessionId,
        Long userId,
        Long skuId,
        Long addressId
) {
}
