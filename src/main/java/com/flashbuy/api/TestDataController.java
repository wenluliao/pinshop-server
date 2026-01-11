package com.flashbuy.api;

import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 临时测试数据Controller
 * 用于在没有数据库连接的情况下测试前端功能
 */
// Temporarily disabled due to route conflict with ProductController
// @RestController
// @RequestMapping("/api/v1/product")
public class TestDataController {

    /**
     * 获取秒杀商品列表（测试数据）
     */
    @GetMapping("/flash-list")
    public Result<List<Object>> getFlashList(@RequestParam(required = false) String timeSlot) {
        List<Object> products = new ArrayList<>();

        // iPhone 15 Pro Max
        products.add(createProduct(1L, "iPhone 15 Pro Max 256GB", 6999.00, 8999.00,
            "https://picsum.photos/400/400?random=101", 20, 50));

        // 小米14 Ultra
        products.add(createProduct(3L, "小米14 Ultra 512GB", 4999.00, 5999.00,
            "https://picsum.photos/400/400?random=102", 50, 100));

        // Tesla玩具车
        products.add(createProduct(5L, "Tesla Model Y 玩具车", 199.00, 299.00,
            "https://picsum.photos/400/400?random=103", 100, 200));

        // 戴森吹风机
        products.add(createProduct(6L, "戴森吹风机 HD08", 2199.00, 2990.00,
            "https://picsum.photos/400/400?random=104", 30, 60));

        // AirPods Pro 2
        products.add(createProduct(7L, "AirPods Pro 2", 1399.00, 1699.00,
            "https://picsum.photos/400/400?random=105", 60, 150));

        // SK-II神仙水
        products.add(createProduct(8L, "SK-II 神仙水 230ml", 890.00, 1290.00,
            "https://picsum.photos/400/400?random=106", 40, 90));

        // 索尼耳机
        products.add(createProduct(11L, "索尼WH-1000XM5耳机", 1599.00, 1999.00,
            "https://picsum.photos/400/400?random=109", 35, 85));

        return Result.ok(products);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{skuId}")
    public Result<Object> getProductDetail(@PathVariable Long skuId) {
        Map<String, Object> product = new HashMap<>();
        product.put("skuId", skuId);
        product.put("name", "iPhone 15 Pro Max 256GB");
        product.put("flashPrice", 6999.00);
        product.put("marketPrice", 9999.00);
        product.put("salePrice", 8999.00);
        product.put("stock", 20);
        product.put("totalStock", 50);
        product.put("sold", 30);
        product.put("mainImage", "https://picsum.photos/400/400?random=101");
        product.put("detailImages", Arrays.asList(
            "https://picsum.photos/750/750?random=101",
            "https://picsum.photos/750/750?random=102",
            "https://picsum.photos/750/750?random=103"
        ));
        product.put("flashEndTime", System.currentTimeMillis() + 3600000);
        product.put("limitPerUser", 1);
        product.put("description", "A17 Pro芯片，钛金属边框，8K视频拍摄");

        return Result.ok(product);
    }

    /**
     * 获取商品分类
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("id", 1);
        cat1.put("name", "数码产品");
        cat1.put("icon", "📱");
        categories.add(cat1);

        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("id", 2);
        cat2.put("name", "玩具模型");
        cat2.put("icon", "🚗");
        categories.add(cat2);

        Map<String, Object> cat3 = new HashMap<>();
        cat3.put("id", 3);
        cat3.put("name", "家用电器");
        cat3.put("icon", "🌀");
        categories.add(cat3);

        Map<String, Object> cat4 = new HashMap<>();
        cat4.put("id", 4);
        cat4.put("name", "美妆护肤");
        cat4.put("icon", "💄");
        categories.add(cat4);

        Map<String, Object> cat5 = new HashMap<>();
        cat5.put("id", 5);
        cat5.put("name", "服饰鞋包");
        cat5.put("icon", "👟");
        categories.add(cat5);

        return Result.ok(categories);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> searchProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize) {

        List<Object> products = new ArrayList<>();
        products.add(createProduct(1L, "iPhone 15 Pro Max 256GB", 6999.00, 8999.00,
            "https://picsum.photos/400/400?random=101", 20, 50));
        products.add(createProduct(3L, "小米14 Ultra 512GB", 4999.00, 5999.00,
            "https://picsum.photos/400/400?random=102", 50, 100));

        Map<String, Object> result = new HashMap<>();
        result.put("list", products);
        result.put("total", 2);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return Result.ok(result);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<Object>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Object> products = new ArrayList<>();

        products.add(createProduct(5L, "Tesla Model Y 玩具车", 199.00, 299.00,
            "https://picsum.photos/400/400?random=103", 100, 200));
        products.add(createProduct(7L, "AirPods Pro 2", 1399.00, 1699.00,
            "https://picsum.photos/400/400?random=105", 60, 150));

        return Result.ok(products);
    }

    /**
     * 创建商品对象
     */
    private Map<String, Object> createProduct(Long skuId, String name, double flashPrice,
                                              double salePrice, String image, int stock, int totalStock) {
        Map<String, Object> product = new HashMap<>();
        product.put("skuId", skuId);
        product.put("name", name);
        product.put("flashPrice", flashPrice);
        product.put("salePrice", salePrice);
        product.put("marketPrice", salePrice * 1.1);
        product.put("mainImage", image);
        product.put("stock", stock);
        product.put("totalStock", totalStock);
        product.put("sold", totalStock - stock);
        product.put("flashEndTime", System.currentTimeMillis() + 3600000);
        product.put("limitPerUser", 1);
        product.put("progress", (int)((totalStock - stock) * 100.0 / totalStock));
        return product;
    }
}
