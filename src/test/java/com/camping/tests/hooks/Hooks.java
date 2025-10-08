package com.camping.tests.hooks;

import com.camping.tests.CommonContext;
import com.camping.tests.util.DatabaseUtils;
import io.cucumber.java.Before;

public class Hooks {
    @Before
    public void beforeScenario() {
        System.out.println("시나리오 시작 전 실행 - 컨텍스트 및 DB 초기화");
        CommonContext.clear();
        // 데이터베이스 클리어 (시나리오 간 데이터 격리)
        DatabaseUtils.clearAll();

        // 시드 데이터 삽입
        DatabaseUtils.seedDefaultData();
    }
}
