package com.onepiece.test;

import com.onepiece.infrastructure.dao.IAiAgentFlowConfigDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Test
    public void test() {
        log.info("测试完成");
    }

    @Test
    public void test2() {
        System.out.println(aiAgentFlowConfigDao.queryAllWithModelName());
    }

}
