package com.flashbuy.application.groupbuy;

import com.flashbuy.common.BusinessException;
import com.flashbuy.domain.marketing.entity.GroupRule;
import com.flashbuy.domain.marketing.entity.GroupSession;
import com.flashbuy.domain.marketing.mapper.GroupRuleMapper;
import com.flashbuy.domain.marketing.mapper.GroupSessionMapper;
import com.flashbuy.domain.trade.entity.TradeOrder;
import com.flashbuy.domain.trade.mapper.TradeOrderMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Group Buy Service
 * Implements group buying logic with social viral mechanics
 */
@Service
public class GroupBuyService {

    private static final Logger log = LoggerFactory.getLogger(GroupBuyService.class);

    private final GroupRuleMapper groupRuleMapper;
    private final GroupSessionMapper groupSessionMapper;
    private final TradeOrderMapper tradeOrderMapper;

    private final AtomicLong orderIdGenerator = new AtomicLong(20000);
    private final AtomicLong sessionIdGenerator = new AtomicLong(1000);

    private static final int STATUS_IN_PROGRESS = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;

    public GroupBuyService(
            GroupRuleMapper groupRuleMapper,
            GroupSessionMapper groupSessionMapper,
            TradeOrderMapper tradeOrderMapper) {
        this.groupRuleMapper = groupRuleMapper;
        this.groupSessionMapper = groupSessionMapper;
        this.tradeOrderMapper = tradeOrderMapper;
    }

    /**
     * Initiate a group (create new group session)
     * Called when first user pays for a group buy item
     */
    @Transactional(rollbackFor = Exception.class)
    public Long initiateGroup(Long ruleId, Long userId, Long skuId) {
        // Step 1: Get group rule
        GroupRule rule = groupRuleMapper.selectOneById(ruleId);
        if (rule == null || rule.getStatus() != 1) {
            throw new BusinessException("Group rule not available");
        }

        // Step 2: Create order
        TradeOrder order = createOrder(userId, ruleId, skuId, rule.getGroupPrice());
        tradeOrderMapper.insert(order);

        // Step 3: Create group session
        GroupSession session = new GroupSession();
        session.setId(sessionIdGenerator.incrementAndGet());
        session.setRuleId(ruleId);
        session.setInitiatorId(userId);
        session.setStatus(STATUS_IN_PROGRESS);
        session.setCurrentCount(1);
        session.setExpireTime(LocalDateTime.now().plusHours(rule.getDurationHours()));
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        groupSessionMapper.insert(session);

        log.info("Group initiated: sessionId={}, userId={}, skuId={}", session.getId(), userId, skuId);

        return session.getId();
    }

    /**
     * Join an existing group
     * Called when subsequent users pay to join
     */
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long sessionId, Long userId, Long skuId) {
        // Step 1: Get session
        GroupSession session = groupSessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new BusinessException("Group session not found");
        }

        // Step 2: Validate session
        if (session.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException("Group is " + (session.getStatus() == STATUS_SUCCESS ? "completed" : "expired"));
        }

        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Group has expired");
        }

        // Step 3: Get rule
        GroupRule rule = groupRuleMapper.selectOneById(session.getRuleId());
        if (rule == null) {
            throw new BusinessException("Group rule not found");
        }

        if (session.getCurrentCount() >= rule.getMemberCount()) {
            throw new BusinessException("Group is already full");
        }

        // Step 4: Create order
        TradeOrder order = createOrder(userId, sessionId, skuId, rule.getGroupPrice());
        tradeOrderMapper.insert(order);

        // Step 5: Update session count
        session.setCurrentCount(session.getCurrentCount() + 1);
        session.setUpdateTime(LocalDateTime.now());
        groupSessionMapper.update(session);

        log.info("User joined group: sessionId={}, userId={}, currentCount={}",
                sessionId, userId, session.getCurrentCount());

        // Step 6: Check if group is complete
        if (session.getCurrentCount() >= rule.getMemberCount()) {
            completeGroup(session);
        }
    }

    /**
     * Complete group session
     * Triggered when group reaches required member count
     */
    private void completeGroup(GroupSession session) {
        session.setStatus(STATUS_SUCCESS);
        session.setUpdateTime(LocalDateTime.now());
        groupSessionMapper.update(session);

        log.info("Group completed: sessionId={}", session.getId());

        // TODO: Send notification to all members
        // TODO: Push orders to warehouse
    }

    /**
     * Create order helper method
     */
    private TradeOrder createOrder(Long userId, Long marketingId, Long skuId, BigDecimal price) {
        TradeOrder order = new TradeOrder();
        order.setId(orderIdGenerator.incrementAndGet());
        order.setUserId(userId);
        order.setTotalAmount(price);
        order.setPayAmount(price);
        order.setStatus(20); // 20: Paid/GroupSuccess
        order.setOrderType("GROUP");
        order.setMarketingId(marketingId);
        order.setReceiverInfo("{\"name\":\"User\",\"phone\":\"13800138000\"}");
        order.setCreateTime(LocalDateTime.now());
        order.setExtraJson("{\"skuId\":" + skuId + "}");
        return order;
    }

    /**
     * Get active group sessions for a SKU
     * Returns groups that are currently in progress
     *
     * @param skuId Product SKU ID
     * @return List of active group sessions
     */
    public List<GroupSessionDto> getActiveGroupSessions(Long skuId) {
        log.info("Fetching active group sessions for skuId={}", skuId);

        // Find active group rules for this SKU
        List<GroupRule> rules = groupRuleMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(GroupRule::getSkuId).eq(skuId)
                        .and(GroupRule::getStatus).eq(1)
        );

        List<GroupSessionDto> result = new ArrayList<>();

        for (GroupRule rule : rules) {
            // Find active sessions for this rule
            List<GroupSession> sessions = groupSessionMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(GroupSession::getRuleId).eq(rule.getId())
                            .and(GroupSession::getStatus).eq(STATUS_IN_PROGRESS)
                            .and(GroupSession::getExpireTime).gt(LocalDateTime.now())
                            .orderBy(GroupSession::getCreateTime, false)
                            .limit(10)
            );

            for (GroupSession session : sessions) {
                // Calculate remaining time
                long timeLeft = ChronoUnit.MILLIS.between(LocalDateTime.now(), session.getExpireTime());

                // Calculate missing members
                int missingNum = rule.getMemberCount() - session.getCurrentCount();

                // Mask username
                String userName = "用户" + (session.getInitiatorId() % 1000);

                result.add(new GroupSessionDto(
                        session.getId(),
                        "https://api.dicebear.com/7.x/avataaars/svg?seed=" + session.getInitiatorId(),
                        userName,
                        missingNum,
                        timeLeft
                ));
            }
        }

        log.info("Found {} active group sessions", result.size());
        return result;
    }

    /**
     * Get user's group buy records
     *
     * @param userId user ID
     * @param pageNum page number
     * @param pageSize page size
     * @return user's group buy records
     */
    public MyGroupsResponse getMyGroups(Long userId, Integer pageNum, Integer pageSize) {
        log.info("Get user's group buy records, userId={}", userId);

        // Find orders where user participated in group buy
        List<TradeOrder> orders = tradeOrderMapper.selectListByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
                .and(TradeOrder::getOrderType).eq("GROUP")
                .orderBy(TradeOrder::getCreateTime, false)
                .limit(pageSize)
                .offset((pageNum - 1) * pageSize)
        );

        int total = Math.toIntExact(tradeOrderMapper.selectCountByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getUserId).eq(userId)
                .and(TradeOrder::getOrderType).eq("GROUP")
        ));

        List<MyGroupDetail> groups = new ArrayList<>();
        for (TradeOrder order : orders) {
            Long sessionId = order.getMarketingId();
            GroupSession session = groupSessionMapper.selectOneById(sessionId);

            if (session != null) {
                GroupRule rule = groupRuleMapper.selectOneById(session.getRuleId());
                String statusText = switch (session.getStatus()) {
                    case STATUS_IN_PROGRESS -> "拼团中";
                    case STATUS_SUCCESS -> "拼团成功";
                    case STATUS_FAILED -> "拼团失败";
                    default -> "未知状态";
                };

                long timeLeft = session.getStatus() == STATUS_IN_PROGRESS
                    ? ChronoUnit.MILLIS.between(LocalDateTime.now(), session.getExpireTime())
                    : 0;

                groups.add(new MyGroupDetail(
                    session.getId(),
                    rule.getSkuId(),
                    session.getStatus(),
                    statusText,
                    session.getCurrentCount(),
                    rule.getMemberCount(),
                    timeLeft,
                    session.getCreateTime()
                ));
            }
        }

        return new MyGroupsResponse(total, groups);
    }

    /**
     * Get group session detail
     *
     * @param sessionId group session ID
     * @return group session detail
     */
    public GroupDetailResponse getGroupDetail(Long sessionId) {
        log.info("Get group detail, sessionId={}", sessionId);

        GroupSession session = groupSessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new BusinessException(4004, "Group session not found");
        }

        GroupRule rule = groupRuleMapper.selectOneById(session.getRuleId());
        if (rule == null) {
            throw new BusinessException(4004, "Group rule not found");
        }

        // Get all orders for this group session
        List<TradeOrder> orders = tradeOrderMapper.selectListByQuery(
            QueryWrapper.create()
                .where(TradeOrder::getMarketingId).eq(sessionId)
                .and(TradeOrder::getOrderType).eq("GROUP")
        );

        List<GroupMember> members = orders.stream()
            .map(order -> new GroupMember(
                order.getUserId(),
                "用户" + (order.getUserId() % 1000),
                "https://api.dicebear.com/7.x/avataaars/svg?seed=" + order.getUserId(),
                order.getId().equals(session.getId()) // Initiator
            ))
            .toList();

        String statusText = switch (session.getStatus()) {
            case STATUS_IN_PROGRESS -> "拼团中";
            case STATUS_SUCCESS -> "拼团成功";
            case STATUS_FAILED -> "拼团失败";
            default -> "未知状态";
        };

        long timeLeft = session.getStatus() == STATUS_IN_PROGRESS
            ? ChronoUnit.MILLIS.between(LocalDateTime.now(), session.getExpireTime())
            : 0;

        return new GroupDetailResponse(
            session.getId(),
            rule.getSkuId(),
            session.getStatus(),
            statusText,
            session.getCurrentCount(),
            rule.getMemberCount(),
            rule.getMemberCount() - session.getCurrentCount(),
            timeLeft,
            session.getExpireTime(),
            members
        );
    }

    // DTOs
    public record MyGroupsResponse(
        Integer total,
        List<MyGroupDetail> groups
    ) {}

    public record MyGroupDetail(
        Long sessionId,
        Long skuId,
        Integer status,
        String statusText,
        Integer currentCount,
        Integer targetCount,
        Long timeLeft,
        LocalDateTime createTime
    ) {}

    public record GroupDetailResponse(
        Long sessionId,
        Long skuId,
        Integer status,
        String statusText,
        Integer currentCount,
        Integer targetCount,
        Integer missingCount,
        Long timeLeft,
        LocalDateTime expireTime,
        List<GroupMember> members
    ) {}

    public record GroupMember(
        Long userId,
        String userName,
        String userAvatar,
        Boolean isInitiator
    ) {}
}
