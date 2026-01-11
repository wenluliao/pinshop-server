package com.flashbuy.domain.trade.repository;

import com.flashbuy.domain.trade.entity.TradeOrder;
import com.flashbuy.domain.trade.mapper.TradeOrderMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Trade Order Repository
 */
@Repository
public class TradeOrderRepository {

    private final TradeOrderMapper tradeOrderMapper;

    public TradeOrderRepository(TradeOrderMapper tradeOrderMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
    }

    /**
     * Calculate today's GMV (Gross Merchandise Value)
     */
    public BigDecimal sumTodayGMV() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Simple query - in production use proper aggregation
        // SELECT COALESCE(SUM(pay_amount), 0) FROM trade_order WHERE create_time >= ? AND create_time < ?
        return tradeOrderMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(TradeOrder::getCreateTime).ge(startOfDay)
                        .and(TradeOrder::getCreateTime).lt(endOfDay)
                        .and(TradeOrder::getStatus).ge(20) // Paid or higher
        ).stream()
                .map(TradeOrder::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Count pending shipments
     */
    public Long countPendingShipments() {
        return tradeOrderMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(TradeOrder::getStatus).eq(20) // Paid/GroupSuccess, waiting for shipment
        );
    }

    public TradeOrder findById(Long id) {
        return tradeOrderMapper.selectOneById(id);
    }

    public java.util.List<TradeOrder> findByUserId(Long userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return tradeOrderMapper.selectListByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
                .orderBy(TradeOrder::getCreateTime, false)
                .limit(pageSize)
                .offset(offset)
        );
    }

    public java.util.List<TradeOrder> findByUserIdAndStatus(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return tradeOrderMapper.selectListByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
                .and(TradeOrder::getStatus).eq(status)
                .orderBy(TradeOrder::getCreateTime, false)
                .limit(pageSize)
                .offset(offset)
        );
    }

    public int countByUserId(Long userId) {
        return Math.toIntExact(tradeOrderMapper.selectCountByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
        ));
    }

    public int countByUserIdAndStatus(Long userId, Integer status) {
        return Math.toIntExact(tradeOrderMapper.selectCountByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
                .and(TradeOrder::getStatus).eq(status)
        ));
    }

    public boolean update(TradeOrder order) {
        return tradeOrderMapper.update(order) > 0;
    }
}
