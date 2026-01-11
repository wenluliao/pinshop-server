package com.flashbuy.domain.item.repository;

import com.flashbuy.domain.item.entity.ProductSku;
import com.flashbuy.domain.item.mapper.ProductSkuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product SKU Repository
 */
@Repository
public class ProductSkuRepository {

    private final ProductSkuMapper productSkuMapper;

    public ProductSkuRepository(ProductSkuMapper productSkuMapper) {
        this.productSkuMapper = productSkuMapper;
    }

    /**
     * Get SKUs with low stock (less than 10)
     */
    public List<ProductSku> getLowStockSkus() {
        return productSkuMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(ProductSku::getStock).lt(10)
                        .limit(100)
        );
    }

    /**
     * Count low stock items
     */
    public Long countLowStock() {
        return productSkuMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(ProductSku::getStock).lt(10)
        );
    }
}
