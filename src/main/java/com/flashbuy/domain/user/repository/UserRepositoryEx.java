package com.flashbuy.domain.user.repository;

import com.flashbuy.domain.user.entity.User;
import com.flashbuy.domain.user.mapper.UserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Repository Extension for Admin
 */
@Repository
public class UserRepositoryEx {

    private final UserMapper userMapper;

    public UserRepositoryEx(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Count new users registered today
     */
    public Long countNewUsersToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return userMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(User::getCreateTime).ge(startOfDay)
                        .and(User::getCreateTime).lt(endOfDay)
        );
    }
}
