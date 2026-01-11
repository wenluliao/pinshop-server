package com.flashbuy.application.product;

/**
 * 秒杀商品信息对象
 *
 * <p>用于首页展示的秒杀商品卡片数据</p>
 *
 * @param skuId 商品SKU编号
 * @param title 商品标题
 * @param imgUrl 商品主图URL
 * @param flashPrice 秒杀价格
 * @param originalPrice 原价（划线价）
 * @param stockPercent 剩余库存百分比（0-100，用于前端进度条显示）
 * @param status 状态（1:抢购中 2:已抢光）
 * @author FlashBuy Team
 * @since 1.6.0
 */
public record FlashProductDto(
        Long skuId,
        String title,
        String imgUrl,
        java.math.BigDecimal flashPrice,
        java.math.BigDecimal originalPrice,
        Integer stockPercent,
        Integer status
) {
}
