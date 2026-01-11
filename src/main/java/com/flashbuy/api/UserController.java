package com.flashbuy.api;

import com.flashbuy.application.user.UserService;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * Provides user management APIs including login, profile, and address management
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User login
     * Supports WeChat openid style login
     *
     * @param request login request
     * @return user ID
     */
    @PostMapping("/login")
    public Result<Long> login(@RequestBody LoginRequest request) {
        Long userId = userService.login(request.openid(), request.nickname(), request.avatarUrl());
        return Result.ok(userId);
    }

    /**
     * Get user profile
     *
     * @param userId user ID
     * @return user profile
     */
    @GetMapping("/profile")
    public Result<UserService.UserProfileResponse> getProfile(@RequestParam Long userId) {
        return Result.ok(userService.getProfile(userId));
    }

    /**
     * Update user profile
     *
     * @param request update request
     * @return void
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest request) {
        userService.updateProfile(request.userId(),
            new UserService.UpdateProfileRequest(
                request.nickname(),
                request.phone()
            )
        );
        return Result.ok();
    }

    /**
     * Get user address list
     *
     * @param userId user ID
     * @return address list
     */
    @GetMapping("/addresses")
    public Result<java.util.List<UserService.UserAddressResponse>> getAddresses(@RequestParam Long userId) {
        return Result.ok(userService.getAddresses(userId));
    }

    /**
     * Add user address
     *
     * @param request add request
     * @return address ID
     */
    @PostMapping("/address")
    public Result<Long> addAddress(@RequestBody AddAddressRequest request) {
        Long addressId = userService.addAddress(request.userId(),
            new UserService.AddAddressRequest(
                request.receiverName(),
                request.receiverPhone(),
                request.province(),
                request.city(),
                request.district(),
                request.detailAddr(),
                request.isDefault()
            )
        );
        return Result.ok(addressId);
    }

    /**
     * Update user address
     *
     * @param addressId address ID
     * @param request update request
     * @return void
     */
    @PutMapping("/address/{addressId}")
    public Result<Void> updateAddress(
        @PathVariable Long addressId,
        @RequestBody UpdateAddressRequest request) {
        userService.updateAddress(request.userId(), addressId,
            new UserService.UpdateAddressRequest(
                request.receiverName(),
                request.receiverPhone(),
                request.province(),
                request.city(),
                request.district(),
                request.detailAddr(),
                request.isDefault()
            )
        );
        return Result.ok();
    }

    /**
     * Delete user address
     *
     * @param userId user ID
     * @param addressId address ID
     * @return void
     */
    @DeleteMapping("/address/{addressId}")
    public Result<Void> deleteAddress(
        @RequestParam Long userId,
        @PathVariable Long addressId) {
        userService.deleteAddress(userId, addressId);
        return Result.ok();
    }

    /**
     * Set default address
     *
     * @param userId user ID
     * @param addressId address ID
     * @return void
     */
    @PostMapping("/address/{addressId}/default")
    public Result<Void> setDefaultAddress(
        @RequestParam Long userId,
        @PathVariable Long addressId) {
        userService.setDefaultAddress(userId, addressId);
        return Result.ok();
    }

    // Request DTOs
    public record LoginRequest(
        String openid,
        String nickname,
        String avatarUrl
    ) {}

    public record UpdateProfileRequest(
        Long userId,
        String nickname,
        String phone
    ) {}

    public record AddAddressRequest(
        Long userId,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddr,
        Integer isDefault
    ) {}

    public record UpdateAddressRequest(
        Long userId,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddr,
        Integer isDefault
    ) {}
}
