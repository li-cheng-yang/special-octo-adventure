package com.internship.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.dir:/tmp/backups}")
    private String backupDir;

    @Scheduled(cron = "0 0 3 * * ?")
    public void autoBackup() {
        try {
            String dbName = extractDbName(datasourceUrl);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = String.format("backup_%s_%s.sql", dbName, timestamp);

            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String outputFile = backupPath.resolve(fileName).toString();

            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    dbName
            );
            pb.redirectOutput(new File(outputFile));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("[数据备份] 成功: {}", outputFile);
                cleanupOldBackups(backupPath, 7); // 保留7天
            } else {
                log.error("[数据备份] 失败，退出码: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("[数据备份] 异常: {}", e.getMessage(), e);
        }
    }

    private String extractDbName(String url) {
        // jdbc:mysql://localhost:3306/dbname?...
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.split("\\?")[0];
    }

    private void cleanupOldBackups(Path backupPath, int keepDays) throws Exception {
        File[] files = backupPath.toFile().listFiles();
        if (files == null) return;

        long cutoff = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L);
        for (File file : files) {
            if (file.lastModified() < cutoff) {
                if (file.delete()) {
                    log.info("[数据备份] 删除过期备份: {}", file.getName());
                }
            }
        }
    }
}
