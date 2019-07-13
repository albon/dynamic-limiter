package bj.albon.arith.dynamic.limiter.example.controller;

import bj.albon.arith.dynamic.limiter.example.service.AirlineService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 示例代码：熔断器测试接口
 */
@Controller
public class CircuitBreakerTestController {


    /**
     * 熔断器测试接口
     *
     * @param useCorrectUrl true 表示使用正确的 url。false 表示使用错误的 url，错误 url 调用失败是用来触发熔断的。
     * @return 返回 true 表示正常执行。返回 false 表示触发了熔断，没有实际调用外部接口。
     */
    @RequestMapping("/test/circuitBreaker")
    @ResponseBody
    public Object testCircuitBreaker(@RequestParam("useCorrectUrl") boolean useCorrectUrl) {
        return AirlineService.query(useCorrectUrl);
    }
}
