package com.flashbuy.domain.user.repository;

import com.flashbuy.domain.user.entity.User;
import com.flashbuy.domain.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

/**
 * User Repository
 */
@Repository
public class UserRepository {

    private final UserMapper userMapper;

    public UserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findById(Long id) {
        return userMapper.selectOneById(id);
    }

    public User findByOpenid(String openid) {
        return userMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .where(User::getOpenid).eq(openid)
                        .limit(1)
        );
    }

    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }
}
