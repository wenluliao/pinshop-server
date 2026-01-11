package com.flashbuy.api;

import com.flashbuy.application.seckill.SeckillRequest;
import com.flashbuy.application.seckill.SeckillResponse;
import com.flashbuy.application.seckill.SeckillService;
import com.flashbuy.common.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 交易中心-秒杀服务
 *
 * <p>提供高并发秒杀抢购功能，采用三级缓存机制保障性能</p>
 *
 * @author FlashBuy Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/trade")
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /**
     * 执行秒杀下单
     *
     * <p>
     * 核心高并发接口，采用异步削峰架构。
     * 前端需轮询查询结果。
     * </p>
     *
     * <p>
     * 业务流程：
     * <ul>
     *   <li>1. 本地缓存检查库存（阻挡90%无效请求）</li>
     *   <li>2. Redis Lua脚本原子扣减库存</li>
     *   <li>3. 发送MQ消息异步创建订单</li>
     *   <li>4. 立即返回排队状态</li>
     * </ul>
     * </p>
     *
     * @param request 秒杀请求参数（包含商品ID、数量、用户ID等）
     * @return 包含订单ID或排队状态的响应数据
     * @apiNote 1. 必须先登录；2. 接口含防刷限流；3. 单次限购1件
     */
    @PostMapping("/seckill")
    public Result<SeckillResponse> doSeckill(@RequestBody SeckillRequest request) {
        SeckillResponse response = seckillService.execute(request);
        return Result.ok(response);
    }

    /**
     * 查询秒杀订单状态
     *
     * <p>
     * 前端通过此接口轮询获取秒杀订单的处理结果。
     * 建议轮询间隔：500ms-1000ms。
     * </p>
     *
     * @param queueId 排队凭证ID（从秒杀下单接口返回）
     * @return 订单处理状态（QUEUING排队中/SUCCESS成功/FAIL失败）
     * @apiNote 轮询最多10次，超过仍未返回结果则视为失败
     */
    @GetMapping("/result/{queueId}")
    public Result<SeckillResponse> getResult(@PathVariable String queueId) {
        // TODO: Implement order status query from Redis cache
        return Result.ok(SeckillResponse.queuing());
    }
}
