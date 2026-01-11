package com.flashbuy.application.groupbuy;

/**
 * 拼团会话信息对象
 *
 * <p>用于展示当前正在进行的拼团信息</p>
 *
 * @param sessionId 拼团会话ID
 * @param userAvatar 团长头像URL
 * @param userName 团长昵称（脱敏显示，如"张***"）
 * @param missingNum 还差几人成团
 * @param timeLeft 剩余时间（毫秒）
 * @author FlashBuy Team
 * @since 1.6.0
 */
public record GroupSessionDto(
        Long sessionId,
        String userAvatar,
        String userName,
        Integer missingNum,
        Long timeLeft
) {
}
