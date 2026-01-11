package com.flashbuy.application.groupbuy;

import com.flashbuy.domain.marketing.entity.GroupSession;
import com.flashbuy.domain.marketing.mapper.GroupSessionMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled Job to Check Expired Group Sessions
 * Uses Java 25 Virtual Threads for parallel processing
 */
@Component
public class GroupExpireJob {

    private static final Logger log = LoggerFactory.getLogger(GroupExpireJob.class);

    private final GroupSessionMapper groupSessionMapper;

    private static final int STATUS_IN_PROGRESS = 0;
    private static final int STATUS_FAILED = 2;

    public GroupExpireJob(GroupSessionMapper groupSessionMapper) {
        this.groupSessionMapper = groupSessionMapper;
    }

    /**
     * Check expired groups every minute
     * Uses virtual thread for async execution
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkExpiredGroups() {
        Thread.ofVirtual().start(() -> {
            try {
                List<GroupSession> expiredSessions = findExpiredSessions();

                if (expiredSessions.isEmpty()) {
                    return;
                }

                log.info("Found {} expired group sessions", expiredSessions.size());

                for (GroupSession session : expiredSessions) {
                    expireGroup(session);
                }

            } catch (Exception e) {
                log.error("Error checking expired groups", e);
            }
        });
    }

    /**
     * Find expired group sessions
     */
    private List<GroupSession> findExpiredSessions() {
        return groupSessionMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(GroupSession::getStatus).eq(STATUS_IN_PROGRESS)
                        .and(GroupSession::getExpireTime).lt(LocalDateTime.now())
                        .limit(100)
        );
    }

    /**
     * Expire a group session and trigger refund
     */
    private void expireGroup(GroupSession session) {
        session.setStatus(STATUS_FAILED);
        session.setUpdateTime(LocalDateTime.now());
        groupSessionMapper.update(session);

        log.info("Group session expired: sessionId={}", session.getId());

        // TODO: Trigger auto-refund for all members
        // TODO: Release locked inventory
    }
}
