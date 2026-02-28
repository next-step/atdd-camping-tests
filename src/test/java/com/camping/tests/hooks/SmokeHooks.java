package com.camping.tests.hooks;

import com.camping.tests.support.ServiceWaiter;
import com.camping.tests.support.TestConfig;
import io.cucumber.java.Before;
import io.restassured.RestAssured;

import java.io.IOException;

/**
 * @smoke 시나리오 실행 전 키오스크 준비를 보장한다.
 *
 * 흐름:
 *  1. 키오스크가 이미 떠 있으면 → 그대로 진행
 *  2. 떠 있지 않으면 → docker compose up -d --build 실행
 *     + JVM 종료 시 docker compose down 자동 등록
 *  3. ServiceWaiter로 /health 폴링 → 준비 완료 후 테스트 시작
 */
public class SmokeHooks {

    private static final String COMPOSE_FILE = "infra/docker-compose.yml";

    private static volatile boolean prepared = false;
    private static volatile boolean composeManagedByHook = false;

    @Before("@smoke")
    public void ensureKioskReady() throws Exception {
        if (prepared) return;

        if (isKioskUp()) {
            System.out.println("[SmokeHooks] 키오스크가 이미 실행 중입니다.");
        } else {
            System.out.println("[SmokeHooks] 키오스크가 감지되지 않아 docker compose를 기동합니다.");
            composeUp();
            composeManagedByHook = true;
            registerShutdownHook();
        }

        ServiceWaiter.waitFor(TestConfig.KIOSK_BASE_URL + "/health", 24, 5_000);
        prepared = true;
    }

    private static boolean isKioskUp() {
        try {
            return RestAssured.given()
                    .get(TestConfig.KIOSK_BASE_URL + "/health")
                    .getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static void ensureNetworkExists() throws IOException, InterruptedException {
        // 네트워크가 없으면 생성, 이미 존재하면 무시 (exit code 1 허용)
        String networkName = "atdd-net";
        System.out.println("[SmokeHooks] docker network create " + networkName);
        int exit = new ProcessBuilder("docker", "network", "create", networkName)
                .inheritIO()
                .start()
                .waitFor();
        if (exit == 0) {
            System.out.println("[SmokeHooks] 네트워크 생성 완료: " + networkName);
        } else {
            System.out.println("[SmokeHooks] 네트워크가 이미 존재합니다: " + networkName);
        }
    }

    private static void composeUp() throws IOException, InterruptedException {
        ensureNetworkExists();
        runCompose("up", "-d", "--build");
    }

    private static void composeDown() {
        try {
            runCompose("down");
        } catch (Exception e) {
            System.err.println("[SmokeHooks] docker compose down 실패: " + e.getMessage());
        }
    }

    private static void runCompose(String... subcommand) throws IOException, InterruptedException {
        String[] base = {"docker", "compose", "-f", COMPOSE_FILE};
        String[] cmd = new String[base.length + subcommand.length];
        System.arraycopy(base, 0, cmd, 0, base.length);
        System.arraycopy(subcommand, 0, cmd, base.length, subcommand.length);

        System.out.println("[SmokeHooks] 실행: " + String.join(" ", cmd));
        int exit = new ProcessBuilder(cmd).inheritIO().start().waitFor();
        if (exit != 0) {
            throw new IllegalStateException("docker compose 명령 실패 (exit=" + exit + ")");
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (composeManagedByHook) {
                System.out.println("[SmokeHooks] JVM 종료 → docker compose down");
                composeDown();
            }
        }, "smoke-compose-shutdown"));
    }
}
