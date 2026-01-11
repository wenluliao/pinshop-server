package com.flashbuy.api;

import com.flashbuy.application.product.FlashProductDto;
import com.flashbuy.application.product.ProductService;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品中心-秒杀商品服务
 *
 * <p>提供秒杀商品列表、详情查询等功能</p>
 *
 * @author FlashBuy Team
 * @since 1.6.0
 */
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取秒杀商品流
     *
     * <p>
     * 返回当前可抢购的秒杀商品列表。
     * 前端用于首页展示秒杀商品卡片。
     * </p>
     *
     * @param timeSlot 时间段（可选，如"08:00"、"12:00"、"20:00"）
     * @return 秒杀商品列表，包含商品标题、价格、库存进度等信息
     * @apiNote 建议前端每30秒刷新一次列表，获取最新库存状态
     */
    @GetMapping("/flash-list")
    public Result<List<FlashProductDto>> getFlashList(
            @RequestParam(required = false) String timeSlot) {
        List<FlashProductDto> products = productService.getFlashList(timeSlot);
        return Result.ok(products);
    }

    /**
     * 获取商品详情
     *
     * <p>
     * 返回指定SKU的商品详细信息。
     * </p>
     *
     * @param skuId 商品SKU编号
     * @return 商品详情信息
     */
    @GetMapping("/detail/{skuId}")
    public Result<FlashProductDto> getProductDetail(@PathVariable Long skuId) {
        FlashProductDto product = productService.getFlashProduct(skuId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.ok(product);
    }

    /**
     * 获取商品分类
     *
     * <p>
     * 返回商品分类列表，用于分类导航。
     * </p>
     *
     * @return 商品分类列表
     */
    @GetMapping("/categories")
    public Result<java.util.List<ProductService.CategoryResponse>> getCategories() {
        return Result.ok(productService.getCategories());
    }

    /**
     * 搜索商品
     *
     * <p>
     * 根据关键词搜索商品。
     * </p>
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认20）
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Result<ProductService.SearchResponse> searchProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(productService.searchProducts(keyword, pageNum, pageSize));
    }

    /**
     * 获取热门商品
     *
     * <p>
     * 返回销量最高的热门商品列表。
     * </p>
     *
     * @param limit 返回数量（默认10）
     * @return 热门商品列表
     */
    @GetMapping("/hot")
    public Result<java.util.List<FlashProductDto>> getHotProducts(
        @RequestParam(defaultValue = "10") Integer limit) {
        return Result.ok(productService.getHotProducts(limit));
    }
}
