package com.flashbuy.application.product;

import java.util.List;

/**
 * 秒杀商品信息对象
 *
 * <p>用于首页展示的秒杀商品卡片数据和详情页数据</p>
 *
 * @param skuId 商品SKU编号
 * @param title 商品标题
 * @param subtitle 商品副标题
 * @param imgUrl 商品主图URL
 * @param images 商品图片列表
 * @param flashPrice 秒杀价格
 * @param salePrice 售价
 * @param originalPrice 原价（划线价）
 * @param stockPercent 剩余库存百分比（0-100，用于前端进度条显示）
 * @param stock 库存数量
 * @param sales 销量
 * @param status 状态（1:抢购中 2:已抢光）
 * @param flashEndTime 秒杀结束时间
 * @param tags 商品标签
 * @param content 商品详情内容（HTML）
 * @author FlashBuy Team
 * @since 1.6.0
 */
public record FlashProductDto(
        Long skuId,
        String title,
        String subtitle,
        String imgUrl,
        List<String> images,
        java.math.BigDecimal flashPrice,
        java.math.BigDecimal salePrice,
        java.math.BigDecimal originalPrice,
        Integer stockPercent,
        Integer stock,
        Integer sales,
        Integer status,
        String flashEndTime,
        List<String> tags,
        String content
) {
}
