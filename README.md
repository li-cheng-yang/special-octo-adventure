# 特别章鱼冒险 - 毕业生实习汇报系统

基于 Spring Boot 3 + Vue 3 + MySQL 8 的全栈毕设项目。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite |
| 后端 | Spring Boot 3 + MyBatis |
| 数据库 | MySQL 8 |
| 可视化 | ECharts |

## 功能介绍

| 模块 | 功能说明 |
|------|----------|
| 🔐 用户认证 | JWT Token + Refresh Token 双令牌认证，支持自动刷新登录态 |
| 📋 实习管理 | 状态流转：草稿 → 待审核 → 进行中 → 已完成，支持驳回与重新提交 |
| 📝 汇报管理 | 支持日报/周报/月报，教师端可评分、批注、退回修改 |
| 📍 签到打卡 | GPS 定位 + 自动地址解析，记录打卡时间、位置与轨迹 |
| 🏢 企业管理 | 企业信息 CRUD，支持实习岗位统计与关联查询 |
| 🔔 消息通知 | 实时未读消息提醒，支持一键全标已读 |
| 📊 数据可视化 | ECharts 多维统计看板（实习状态、汇报统计、企业 TOP10 等） |
| 📤 数据导出 | 签到记录、汇报数据、评价结果一键导出 Excel |
| ⭐ 实习评价 | 多维度评分（工作态度、学习能力、团队协作、专业技能等） |
| 🏫 学院隔离 | 教师仅可查看与管理本院系学生，防止跨院数据泄露 |

## 项目结构

```
special-octo-adventure/
├── frontend/          # Vue 3 前端（端口 5173）
│   ├── src/
│   ├── public/
│   └── ...
├── backend/           # Spring Boot 后端（端口 5000）
│   ├── src/main/
│   └── ...
├── MySql/             # 数据库初始化脚本
└── README.md
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < MySql/init.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# 或编译后运行
mvn clean package
java -jar target/backend-*.jar
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问系统

- 前端地址：http://localhost:5173
- 后端地址：http://localhost:5000

## 角色权限

| 角色 | 权限说明 |
|------|----------|
| 学生 | 提交实习申请、撰写汇报、签到打卡、查看评价 |
| 教师 | 审核实习、批改汇报、查看学生签到、多维评价 |
| 管理员 | 用户管理、企业管理、数据统计、系统配置 |

## 许可证

[Unlicense](LICENSE) - 完全开源，无限制使用。
