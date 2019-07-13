package bj.albon.arith.dynamic.limiter.example.controller;

import bj.albon.arith.dynamic.limiter.config.DynamicLimiterConfig;
import bj.albon.arith.dynamic.limiter.example.service.DynamicLimitTestService;
import bj.albon.arith.dynamic.limiter.example.util.DynamicLimiterUtil;
import bj.albon.arith.dynamic.limiter.example.util.ExecutorUtil;
import bj.albon.arith.dynamic.limiter.util.Monitor;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 示例代码：动态限流测试接口
 */
@Controller
public class LoadDynamicLimitTestController {

    /**
     * 手工调用 DynamicLimiter 的动态限流
     *
     * @param num 调一次接口，启动 num 个 DynamicLimitTestService
     * @return true，不被限流。false，被限流。
     */
    @RequestMapping("/test/loadDynamicLimit")
    @ResponseBody
    public Object testLoadDynamicLimit(@RequestParam("num") int num) {
        if (DynamicLimiterUtil.LOAD_DYNAMIC_LIMITER.exceed()) {
            return false;
        }

        long start = System.currentTimeMillis();

        // 调一次接口跑 num 个，让 load 飙起来
        for (int i = 0; i < num; ++i) {
            ExecutorUtil.EXECUTOR.execute(new DynamicLimitTestService());
        }

        Monitor.recordOne("test_loadDynamicLimit", System.currentTimeMillis() - start);
        return true;
    }

    /**
     * 限流后兜底的接口，具体怎么兜底，需要根据实际业务决定。
     *
     * @return false，表示被限流。
     */
    @RequestMapping("/test/extension/rate/limited")
    @ResponseBody
    public Object testExtensionRateLimited() {
        return false;
    }
}
