package bj.albon.arith.dynamic.limit.example.test;

/**
 * 动态限流测试代码
 */
public class TestLoadDynamicLimit extends AbstractTestLoadDynamicLimit {
    // TEST_URL 中的 host 表示部署 example war 包的服务器地址
    private static final String TEST_URL = "http://127.0.0.1:8080/test/loadDynamicLimit?num=5";

    @Override
    protected String getTestUrl() {
        return TEST_URL;
    }
}
