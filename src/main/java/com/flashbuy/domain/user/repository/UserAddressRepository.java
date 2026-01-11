package com.flashbuy.domain.user.repository;

import com.flashbuy.domain.user.entity.UserAddress;
import com.flashbuy.domain.user.mapper.UserAddressMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User Address Repository
 */
@Repository
public class UserAddressRepository {

    private final UserAddressMapper userAddressMapper;

    public UserAddressRepository(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    public UserAddress findById(Long id) {
        return userAddressMapper.selectOneById(id);
    }

    public List<UserAddress> findByUserId(Long userId) {
        return userAddressMapper.selectListByQuery(
            com.mybatisflex.core.query.QueryWrapper.create()
                .where(UserAddress::getUserId).eq(userId)
                .orderBy(UserAddress::getIsDefault, false)
                .orderBy(UserAddress::getCreateTime, false)
        );
    }

    public Long save(UserAddress address) {
        userAddressMapper.insert(address);
        return address.getId();
    }

    public boolean update(UserAddress address) {
        return userAddressMapper.update(address) > 0;
    }

    public boolean deleteById(Long id) {
        return userAddressMapper.deleteById(id) > 0;
    }

    public void clearDefault(Long userId) {
        // Find all default addresses and update them
        java.util.List<UserAddress> addresses = userAddressMapper.selectListByQuery(
            com.mybatisflex.core.query.QueryWrapper.create()
                .where(UserAddress::getUserId).eq(userId)
                .and(UserAddress::getIsDefault).eq(1)
        );

        for (UserAddress addr : addresses) {
            addr.setIsDefault(0);
            userAddressMapper.update(addr);
        }
    }
}
