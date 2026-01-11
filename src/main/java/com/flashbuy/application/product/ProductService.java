package com.flashbuy.application.product;

import com.flashbuy.domain.item.entity.FlashItem;
import com.flashbuy.domain.item.entity.ProductSku;
import com.flashbuy.domain.item.entity.ProductSpu;
import com.flashbuy.domain.item.mapper.FlashItemMapper;
import com.flashbuy.domain.item.mapper.ProductSkuMapper;
import com.flashbuy.domain.item.mapper.ProductSpuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务
 *
 * <p>提供秒杀商品查询、列表展示等功能</p>
 *
 * @author FlashBuy Team
 * @since 1.6.0
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final FlashItemMapper flashItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;

    public ProductService(
            FlashItemMapper flashItemMapper,
            ProductSkuMapper productSkuMapper,
            ProductSpuMapper productSpuMapper) {
        this.flashItemMapper = flashItemMapper;
        this.productSkuMapper = productSkuMapper;
        this.productSpuMapper = productSpuMapper;
    }

    /**
     * 获取秒杀商品列表
     *
     * @param timeSlot 时间段（如"08:00"，可选）
     * @return 秒杀商品列表
     */
    public List<FlashProductDto> getFlashList(String timeSlot) {
        log.info("Fetching flash product list for timeSlot={}", timeSlot);

        // 查询所有秒杀商品
        List<FlashItem> flashItems = flashItemMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(FlashItem::getFlashStock).gt(0)
                        .orderBy(FlashItem::getEventId, true)
                        .limit(50)
        );

        List<FlashProductDto> result = new ArrayList<>();

        for (FlashItem flashItem : flashItems) {
            // 查询SKU信息
            ProductSku sku = productSkuMapper.selectOneById(flashItem.getSkuId());
            if (sku == null) {
                continue;
            }

            // 查询SPU信息
            ProductSpu spu = productSpuMapper.selectOneById(sku.getSpuId());
            if (spu == null) {
                continue;
            }

            // 计算库存百分比
            int stockPercent = (flashItem.getFlashStock() - flashItem.getLockStock()) * 100 / flashItem.getFlashStock();
            int status = (flashItem.getFlashStock() - flashItem.getLockStock()) > 0 ? 1 : 2;

            result.add(new FlashProductDto(
                    sku.getId(),
                    spu.getName(),
                    "", // subtitle
                    spu.getMainImage() != null ? spu.getMainImage() : "",
                    List.of(spu.getMainImage()), // images
                    flashItem.getFlashPrice(),
                    sku.getSalePrice() != null ? sku.getSalePrice() : sku.getMarketPrice(), // salePrice
                    sku.getMarketPrice(),
                    stockPercent,
                    flashItem.getFlashStock() - flashItem.getLockStock(), // stock
                    0, // sales - TODO: from order statistics
                    status,
                    "", // flashEndTime
                    List.of("秒杀", "限时"), // tags
                    ""  // content
            ));
        }

        log.info("Found {} flash products", result.size());
        return result;
    }

    /**
     * 根据SKU ID获取商品详情
     *
     * @param skuId SKU编号
     * @return 商品详情
     */
    public FlashProductDto getFlashProduct(Long skuId) {
        log.info("Fetching flash product details for skuId={}", skuId);

        ProductSku sku = productSkuMapper.selectOneById(skuId);
        if (sku == null) {
            return null;
        }

        ProductSpu spu = productSpuMapper.selectOneById(sku.getSpuId());
        if (spu == null) {
            return null;
        }

        FlashItem flashItem = flashItemMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(FlashItem::getSkuId).eq(skuId)
                        .limit(1)
        );

        if (flashItem == null) {
            return null;
        }

        // 解析图片列表
        List<String> images = parseImages(spu.getDetailImages());

        // 构建商品详情
        int stockPercent = (flashItem.getFlashStock() - flashItem.getLockStock()) * 100 / flashItem.getFlashStock();
        int status = (flashItem.getFlashStock() - flashItem.getLockStock()) > 0 ? 1 : 2;

        // 模拟商品详情HTML内容
        String content = buildProductContent(spu);

        return new FlashProductDto(
                sku.getId(),
                spu.getName(),
                spu.getName() + " - " + sku.getSpecs(), // subtitle
                spu.getMainImage() != null ? spu.getMainImage() : "",
                images,
                flashItem.getFlashPrice(),
                sku.getSalePrice() != null ? sku.getSalePrice() : sku.getMarketPrice(), // salePrice
                sku.getMarketPrice(),
                stockPercent,
                flashItem.getFlashStock() - flashItem.getLockStock(), // stock
                0, // sales - TODO: from order statistics
                status,
                "", // flashEndTime - TODO: from flash_event.end_time
                List.of("秒杀", "限时", "热卖"), // tags
                content
        );
    }

    /**
     * 解析图片列表
     */
    private List<String> parseImages(String detailImages) {
        if (detailImages == null || detailImages.isEmpty()) {
            return List.of();
        }
        try {
            // detailImages is a JSON string like "[\"url1\",\"url2\"]"
            if (detailImages.startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(
                    detailImages,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
            }
        } catch (Exception e) {
            log.warn("Failed to parse detail images: {}", detailImages, e);
        }
        return List.of(detailImages);
    }

    /**
     * 构建商品详情HTML内容
     */
    private String buildProductContent(ProductSpu spu) {
        return """
            <div class="product-detail">
                <h2>商品介绍</h2>
                <p>%s</p>
                <h3>产品特点</h3>
                <ul>
                    <li>正品保证</li>
                    <li>极速发货</li>
                    <li>7天无理由退换</li>
                </ul>
            </div>
            """.formatted(spu.getName());
    }

    /**
     * Get product categories
     *
     * @return category list
     */
    public List<CategoryResponse> getCategories() {
        log.info("Get product categories");

        // Mock data - in production, query from category table
        List<CategoryResponse> categories = new ArrayList<>();
        categories.add(new CategoryResponse(1L, "数码电器", "https://cdn.pinshop.com/categories/1.png"));
        categories.add(new CategoryResponse(2L, "居家生活", "https://cdn.pinshop.com/categories/2.png"));
        categories.add(new CategoryResponse(3L, "服饰鞋包", "https://cdn.pinshop.com/categories/3.png"));
        categories.add(new CategoryResponse(4L, "美妆个护", "https://cdn.pinshop.com/categories/4.png"));
        categories.add(new CategoryResponse(5L, "食品生鲜", "https://cdn.pinshop.com/categories/5.png"));

        return categories;
    }

    /**
     * Search products by keyword
     *
     * @param keyword search keyword
     * @param pageNum page number
     * @param pageSize page size
     * @return search results
     */
    public SearchResponse searchProducts(String keyword, Integer pageNum, Integer pageSize) {
        log.info("Search products, keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

        // Search in SPU
        List<ProductSpu> spus = productSpuMapper.selectListByQuery(
            QueryWrapper.create()
                .where(ProductSpu::getName).like(keyword)
                .orderBy(ProductSpu::getId, false)
                .limit(pageSize)
                .offset((pageNum - 1) * pageSize)
        );

        int total = Math.toIntExact(productSpuMapper.selectCountByQuery(
            QueryWrapper.create()
                .where(ProductSpu::getName).like(keyword)
        ));

        List<FlashProductDto> products = new ArrayList<>();
        for (ProductSpu spu : spus) {
            // Get first SKU
            List<ProductSku> skus = productSkuMapper.selectListByQuery(
                QueryWrapper.create()
                    .where(ProductSku::getSpuId).eq(spu.getId())
                    .limit(1)
            );

            if (!skus.isEmpty()) {
                ProductSku sku = skus.get(0);

                // Check if has flash item
                FlashItem flashItem = flashItemMapper.selectOneByQuery(
                    QueryWrapper.create()
                        .where(FlashItem::getSkuId).eq(sku.getId())
                        .limit(1)
                );

                java.math.BigDecimal price = flashItem != null ? flashItem.getFlashPrice() : sku.getMarketPrice();
                int stockPercent = flashItem != null ? 100 : 0;
                int status = flashItem != null && flashItem.getFlashStock() > 0 ? 1 : 2;

                products.add(new FlashProductDto(
                    sku.getId(),
                    spu.getName(),
                    "", // subtitle
                    spu.getMainImage() != null ? spu.getMainImage() : "",
                    List.of(spu.getMainImage()), // images
                    price,
                    sku.getSalePrice() != null ? sku.getSalePrice() : sku.getMarketPrice(),
                    sku.getMarketPrice(),
                    stockPercent,
                    flashItem != null ? flashItem.getFlashStock() : 0, // stock
                    0, // sales
                    status,
                    "", // flashEndTime
                    List.of(), // tags
                    ""  // content
                ));
            }
        }

        return new SearchResponse(total, products);
    }

    /**
     * Get hot products
     *
     * @param limit limit count
     * @return hot product list
     */
    public List<FlashProductDto> getHotProducts(Integer limit) {
        log.info("Get hot products, limit={}", limit);

        List<ProductSpu> spus = productSpuMapper.selectListByQuery(
            QueryWrapper.create()
                .orderBy(ProductSpu::getId, false)
                .limit(limit)
        );

        List<FlashProductDto> products = new ArrayList<>();
        for (ProductSpu spu : spus) {
            List<ProductSku> skus = productSkuMapper.selectListByQuery(
                QueryWrapper.create()
                    .where(ProductSku::getSpuId).eq(spu.getId())
                    .limit(1)
            );

            if (!skus.isEmpty()) {
                ProductSku sku = skus.get(0);

                FlashItem flashItem = flashItemMapper.selectOneByQuery(
                    QueryWrapper.create()
                        .where(FlashItem::getSkuId).eq(sku.getId())
                        .limit(1)
                );

                java.math.BigDecimal price = flashItem != null ? flashItem.getFlashPrice() : sku.getMarketPrice();
                int stockPercent = flashItem != null && flashItem.getFlashStock() > 0 ? 100 : 0;
                int status = flashItem != null && flashItem.getFlashStock() > 0 ? 1 : 2;

                products.add(new FlashProductDto(
                    sku.getId(),
                    spu.getName(),
                    "", // subtitle
                    spu.getMainImage() != null ? spu.getMainImage() : "",
                    List.of(spu.getMainImage()), // images
                    price,
                    sku.getSalePrice() != null ? sku.getSalePrice() : sku.getMarketPrice(),
                    sku.getMarketPrice(),
                    stockPercent,
                    flashItem != null ? flashItem.getFlashStock() : 0, // stock
                    0, // sales
                    status,
                    "", // flashEndTime
                    List.of(), // tags
                    ""  // content
                ));
            }
        }

        return products;
    }

    // DTOs
    public record CategoryResponse(
        Long categoryId,
        String categoryName,
        String categoryIcon
    ) {}

    public record SearchResponse(
        Integer total,
        List<FlashProductDto> products
    ) {}
}
