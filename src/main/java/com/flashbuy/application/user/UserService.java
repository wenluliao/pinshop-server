package com.flashbuy.application.user;

import com.flashbuy.common.BusinessException;
import com.flashbuy.domain.user.entity.User;
import com.flashbuy.domain.user.entity.UserAddress;
import com.flashbuy.domain.user.repository.UserRepository;
import com.flashbuy.domain.user.repository.UserAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service
 * Handles user related operations including login, registration, profile management
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;

    public UserService(UserRepository userRepository, UserAddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * User login by openid (WeChat style)
     *
     * @param openid user openid
     * @param nickname user nickname
     * @param avatarUrl user avatar URL
     * @return user ID
     */
    @Transactional
    public Long login(String openid, String nickname, String avatarUrl) {
        log.info("User login, openid={}", openid);

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            // New user, auto register
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setStatus(1);
            userRepository.save(user);
            log.info("New user registered, userId={}", user.getId());
        } else {
            // Update user info
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            userRepository.update(user);
        }

        return user.getId();
    }

    /**
     * Get user profile
     *
     * @param userId user ID
     * @return user profile
     */
    public UserProfileResponse getProfile(Long userId) {
        log.info("Get user profile, userId={}", userId);

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(4002, "User not found");
        }

        return new UserProfileResponse(
            user.getId(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getPhone(),
            user.getStatus()
        );
    }

    /**
     * Update user profile
     *
     * @param userId user ID
     * @param request update request
     */
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Update user profile, userId={}", userId);

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(4002, "User not found");
        }

        user.setNickname(request.nickname());
        user.setPhone(request.phone());
        userRepository.update(user);
    }

    /**
     * Get user address list
     *
     * @param userId user ID
     * @return address list
     */
    public List<UserAddressResponse> getAddresses(Long userId) {
        log.info("Get user addresses, userId={}", userId);

        return addressRepository.findByUserId(userId).stream()
            .map(this::toAddressResponse)
            .toList();
    }

    /**
     * Add user address
     *
     * @param userId user ID
     * @param request address request
     * @return address ID
     */
    @Transactional
    public Long addAddress(Long userId, AddAddressRequest request) {
        log.info("Add user address, userId={}", userId);

        // If set as default, unset other default addresses
        if (request.isDefault() == 1) {
            addressRepository.clearDefault(userId);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetailAddr(request.detailAddr());
        address.setIsDefault(request.isDefault());

        addressRepository.save(address);
        return address.getId();
    }

    /**
     * Update user address
     *
     * @param userId user ID
     * @param addressId address ID
     * @param request update request
     */
    @Transactional
    public void updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        log.info("Update user address, userId={}, addressId={}", userId, addressId);

        UserAddress address = addressRepository.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(4003, "Address not found");
        }

        // If set as default, unset other default addresses
        if (request.isDefault() == 1) {
            addressRepository.clearDefault(userId);
        }

        address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetailAddr(request.detailAddr());
        address.setIsDefault(request.isDefault());

        addressRepository.update(address);
    }

    /**
     * Delete user address
     *
     * @param userId user ID
     * @param addressId address ID
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Delete user address, userId={}, addressId={}", userId, addressId);

        UserAddress address = addressRepository.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(4003, "Address not found");
        }

        addressRepository.deleteById(addressId);
    }

    /**
     * Set default address
     *
     * @param userId user ID
     * @param addressId address ID
     */
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        log.info("Set default address, userId={}, addressId={}", userId, addressId);

        UserAddress address = addressRepository.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(4003, "Address not found");
        }

        addressRepository.clearDefault(userId);
        address.setIsDefault(1);
        addressRepository.update(address);
    }

    private UserAddressResponse toAddressResponse(UserAddress address) {
        return new UserAddressResponse(
            address.getId(),
            address.getReceiverName(),
            address.getReceiverPhone(),
            address.getProvince(),
            address.getCity(),
            address.getDistrict(),
            address.getDetailAddr(),
            address.getIsDefault()
        );
    }

    // DTOs
    public record UserProfileResponse(
        Long userId,
        String nickname,
        String avatarUrl,
        String phone,
        Integer status
    ) {}

    public record UpdateProfileRequest(
        String nickname,
        String phone
    ) {}

    public record UserAddressResponse(
        Long addressId,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddr,
        Integer isDefault
    ) {}

    public record AddAddressRequest(
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddr,
        Integer isDefault
    ) {}

    public record UpdateAddressRequest(
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddr,
        Integer isDefault
    ) {}
}
