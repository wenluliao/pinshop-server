package com.flashbuy.api;

import com.flashbuy.application.groupbuy.GroupBuyService;
import com.flashbuy.application.groupbuy.GroupSessionDto;
import com.flashbuy.application.groupbuy.InitiateGroupRequest;
import com.flashbuy.application.groupbuy.JoinGroupRequest;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团中心-拼团服务
 *
 * <p>提供拼团发起、参与、查询等功能</p>
 *
 * @author FlashBuy Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/group")
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    public GroupBuyController(GroupBuyService groupBuyService) {
        this.groupBuyService = groupBuyService;
    }

    /**
     * 发起拼团
     *
     * <p>
     * 用户成为团长，创建一个新的拼团会话。
     * </p>
     *
     * @param request 拼团请求参数（包含规则ID、用户ID、商品SKU ID、地址ID）
     * @return 拼团会话ID
     * @apiNote 用户支付成功后调用此接口
     */
    @PostMapping("/initiate")
    public Result<Long> initiate(@RequestBody InitiateGroupRequest request) {
        Long sessionId = groupBuyService.initiateGroup(request.ruleId(), request.userId(), request.skuId());
        return Result.ok(sessionId);
    }

    /**
     * 参与拼团
     *
     * <p>
     * 用户加入已有的拼团会话。
     * </p>
     *
     * @param request 拼团请求参数（包含会话ID、用户ID、商品SKU ID、地址ID）
     * @return 成功或失败信息
     * @apiNote 用户支付成功后调用此接口
     */
    @PostMapping("/join")
    public Result<Void> join(@RequestBody JoinGroupRequest request) {
        groupBuyService.joinGroup(request.sessionId(), request.userId(), request.skuId());
        return Result.ok();
    }

    /**
     * 获取当前正在拼的团
     *
     * <p>
     * 返回指定商品正在进行的拼团会话列表。
     * 前端用于展示"还有人在拼单"功能。
     * </p>
     *
     * @param skuId 商品SKU ID
     * @return 拼团会话列表（包含团长头像、剩余人数、剩余时间等）
     * @apiNote 建议前端每10秒刷新一次，获取最新拼团状态
     */
    @GetMapping("/{skuId}/sessions")
    public Result<List<GroupSessionDto>> getActiveSessions(@PathVariable Long skuId) {
        List<GroupSessionDto> sessions = groupBuyService.getActiveGroupSessions(skuId);
        return Result.ok(sessions);
    }

    /**
     * 获取我的拼团记录
     *
     * <p>
     * 返回用户参与的所有拼团记录。
     * </p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 我的拼团记录列表
     */
    @GetMapping("/my")
    public Result<GroupBuyService.MyGroupsResponse> getMyGroups(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(groupBuyService.getMyGroups(userId, pageNum, pageSize));
    }

    /**
     * 获取拼团详情
     *
     * <p>
     * 返回指定拼团会话的详细信息，包括所有成员列表。
     * </p>
     *
     * @param sessionId 拼团会话ID
     * @return 拼团详情
     */
    @GetMapping("/{sessionId}/detail")
    public Result<GroupBuyService.GroupDetailResponse> getGroupDetail(@PathVariable Long sessionId) {
        return Result.ok(groupBuyService.getGroupDetail(sessionId));
    }
}
