package com.flashbuy.application.groupbuy;

/**
 * 发起拼团请求
 *
 * @param ruleId 拼团规则ID
 * @param userId 用户ID
 * @param skuId 商品SKU ID
 * @param addressId 收货地址ID
 */
public record InitiateGroupRequest(
        Long ruleId,
        Long userId,
        Long skuId,
        Long addressId
) {
}
