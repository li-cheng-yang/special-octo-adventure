package com.internship.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@SpringBootApplication
public class InternshipReportApplication {

    public static void main(String[] args) {
        Instant start = Instant.now();
        SpringApplication.run(InternshipReportApplication.class, args);
        Duration startupTime = Duration.between(start, Instant.now());
        log.info("应用启动耗时: {} 秒", startupTime.toMillis() / 1000.0);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener(Environment env) {
        return event -> {
            String port = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String appName = env.getProperty("spring.application.name", "实习管理系统");
            String activeProfile = env.getProperty("spring.profiles.active", "default");

            String host;
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn("无法获取主机地址，使用 localhost: {}", e.getMessage());
                host = "localhost";
            }

            String localUrl = "http://localhost:" + port + contextPath;
            String networkUrl = "http://" + host + ":" + port + contextPath;

            printStartupInfo(appName, localUrl, networkUrl, activeProfile);
        };
    }

    private void printStartupInfo(String appName, String localUrl, String networkUrl, String profile) {
        log.info("\n" +
                        "==================================================================\n" +
                        "  {} 启动成功！\n" +
                        "==================================================================\n" +
                        "  环境: {} | 模式: {}\n" +
                        "------------------------------------------------------------------\n" +
                        "  本机访问: {}\n" +
                        "  局域网访问: {}\n" +
                        "==================================================================\n",
                appName,
                profile.toUpperCase(),
                isDev(profile) ? "开发模式" : "生产模式",
                localUrl,
                networkUrl
        );

        if (isDev(profile)) {
            log.info("开发模式提示：前端默认地址 http://localhost:5173");
        }
    }

    private boolean isDev(String profile) {
        return "dev".equalsIgnoreCase(profile) || "default".equalsIgnoreCase(profile);
    }
}