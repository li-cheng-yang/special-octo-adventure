# 实习汇报系统 - 后端

## 技术栈
- Spring Boot
- MySQL 8.0+
- JWT 认证

## 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE internship_report
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

2. 导入初始化数据：
```bash
mysql -u root -p internship_report < sql/init.sql
```

## 运行

### 开发环境（推荐）
1. 在 `src/main/resources/` 下创建 `application-local.yml`（已配置 `.gitignore` 忽略）
2. 填写本地配置：数据库密码、JWT 密钥等

3. 启动时激活 `local` 环境：
```bash
# 命令行方式
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 或在 IDE 的 Run Configuration → VM Options 中添加：
# -Dspring.profiles.active=local
```

### 默认端口
- 后端服务：`http://localhost:5000`

## 演示账号
- 管理员：admin / 123456
- 教师：teacher1 / 123456
- 学生：student1 / 123456

> ⚠️ 以上账号为测试数据，仅供开发环境使用。
