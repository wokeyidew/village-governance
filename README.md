# 数字乡理 · 乡村智慧治理平台（后端）

> 面向乡村的模块化、可插拔的智慧治理工具集。一期实现党建积分制、积分超市、村务通知、政策推送、活动管理等功能。  
> 多租户SaaS架构，一套代码服务多个村庄，支持热插拔模块、积分规则动态配置、智能预警。

---

## 📖 目录

[TOC]

---

## 项目简介

“数字乡理”是响应广东省“百千万工程”与“万名学子乡村大调研”号召，针对阳江市阳东区那龙镇龙胜村“党建引领积分制数字化升级”真实需求开发的一体化治理平台。平台采用多租户模块化设计，各村可独立启用所需功能，支持积分规则热更新、智能预警、积分超市核销等核心业务。一期已完成积分制闭环和基础村务通知，后续将扩展便民服务、补贴查询、民生报修、村民议事、时间银行等模块。

---

## 技术栈

| 类别         | 技术                   | 版本   | 用途                       |
| ------------ | ---------------------- | ------ | -------------------------- |
| 后端框架     | Spring Boot            | 2.7.18 | 基础容器                   |
| ORM          | MyBatis-Plus           | 3.5.5  | 简化数据库操作，多租户插件 |
| 数据库       | MySQL                  | 8.0+   | 关系数据存储               |
| 安全         | Spring Security + JWT  | -      | 认证授权                   |
| 密码加密     | BCrypt                 | -      | 用户密码加密               |
| 缓存（可选） | Redis                  | 6.x    | 积分规则缓存、分布式锁     |
| 工具库       | Lombok, Hutool         | -      | 简化代码                   |
| 文件存储     | 阿里云OSS              | 3.17.4 | 图片/附件存储              |
| API文档      | Knife4j (Swagger2)     | 4.4.0  | 自动生成接口文档           |
| 健康检查     | Spring Boot Actuator   | -      | 监控与健康探针             |
| 容器化       | Docker, docker-compose | -      | 一键部署                   |
| 日志         | Logback                | -      | 滚动日志文件               |

---

## 系统架构

**多租户 SaaS 三层架构**（前端 H5 + PC 管理后台共用后端 API）

- **村民端（H5）**：积分申报、查询、积分超市、活动报名、通知政策查看、个人中心。
- **管理后台（PC）**：积分规则配置、申报审核、商品管理、核销、村民档案、数据统计、预警中心、模块开关。

**核心设计**

1. **多租户隔离**：所有业务表含 `tenant_id`，MyBatis-Plus 多租户插件自动过滤。超级管理员可管理多村，村级管理员仅见本村。
2. **模块化可插拔**：后台模块开关，村庄可独立启用所需功能。新模块开发后注册即可使用。
3. **积分规则热更新**：规则存储在数据库，前端动态拉取。后台修改分值或指标后立即生效，无需重启服务。
4. **智能预警**：定时任务检测异常（小组长期无申报、参与率骤降等），后台高亮提示并记录预警日志。
5. **乐观锁防超卖**：商品库存使用 `version` 字段，高并发下保证兑换数据正确。
6. **完整积分闭环**：申报 → 审核 → 积分变动 → 查询 → 兑换 → 核销 → 流水记录。

---

## 功能特性

### ✅ 已完成（一期核心）

| 模块     | 功能                                                         | 说明                                                   |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------ |
| 认证授权 | 登录、JWT令牌、BCrypt加密                                    | 支持角色：村民、网格员、村管理员、镇管理员、超级管理员 |
| 积分管理 | 规则配置（热更新）、申报提交、审核（通过/驳回）、积分流水    | 支持每日次数上限、需上传照片等规则                     |
| 积分超市 | 商品列表、兑换、生成核销码（7天有效）、后台扫码核销          | 兑换时扣减库存，库存不足自动下架                       |
| 活动管理 | 村委发布活动、村民报名、取消报名（活动开始前24小时）、签到并自动发放奖励积分 | 支持报名人数上限                                       |
| 村务通知 | 发布通知、置顶、列表查看                                     | 前端可做已读/未读红点                                  |
| 政策推送 | 分类管理（养老、医保、农业补贴等）、搜索筛选                 | 支持附件上传                                           |
| 仪表盘   | 今日申报数、待审核数、总积分、参与人数趋势图                 | ECharts 展示                                           |
| 预警中心 | 自动检测“连续14天无申报”并生成预警记录                       | 村委可标记已处理                                       |
| 多租户   | 超级管理员可增删村庄，村级管理员数据隔离                     | 支持模板库一键导入（预置模板）                         |

### 🚧 计划中（二期/远期）

- 便民服务指南（办事流程图文）
- 补贴查询（对接村委数据）
- 民生报修（拍照上报、派单维修）
- 村民议事（议题投票）
- 随手拍环境整治
- 时间银行（积分兑换邻里服务）
- 公共设施认养

---

## 环境要求

- **JDK 17**（推荐 Temurin 17.0.18+）
- **Maven 3.9+**
- **MySQL 8.0+**（需支持 utf8mb4 字符集）
- **Git**（可选，用于版本管理）
- **Docker 20.10+**（可选，用于容器化部署）
- **Redis 6.x**（可选，用于缓存）

---

## 快速开始（本地开发）

### 1. 克隆代码

```bash
git clone https://github.com/your-group/village-governance.git
cd village-governance
```

### 2. 准备数据库

启动 MySQL，执行以下命令创建数据库并初始化表结构：

```bash
mysql -u root -p < docs/schema.sql
```

如果项目中没有 `docs/schema.sql`，请手动执行以下 SQL（完整建表语句见 [附录](#附录)）：

```sql
CREATE DATABASE IF NOT EXISTS village_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE village_db;
-- 然后执行附录中的所有 CREATE TABLE 语句
```

### 3. 修改配置文件

打开 `src/main/resources/application-dev.yml`，至少修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/village_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码
```

如果需要使用阿里云 OSS，请填写正确的 `access-key-id` 和 `access-key-secret`；如果不需要，可以暂时注释 `OssUtils` 相关调用（不影响积分核心功能）。

### 4. 启动后端服务

```bash
mvn clean spring-boot:run
```

当控制台出现以下日志时表示启动成功：

```
Started VillageApplication in 5.234 seconds (JVM running for 6.123)
```

### 5. 测试接口

访问内置 API 文档：`http://localhost:8080/doc.html`

**登录测试**（需先在数据库 `user` 表中手动插入一个测试用户，密码使用 BCrypt 加密生成）：

```bash
# 生成 BCrypt 密文（临时方案）
# 在 VillageApplication 中临时添加以下代码并运行一次：
@Bean
CommandLineRunner temp(BCryptPasswordEncoder encoder) {
    return args -> System.out.println(encoder.encode("123456"));
}
# 控制台会输出类似：$2a$10$NkMpL7K9JqXyZ... 复制该密文
```

插入测试用户 SQL：

```sql
INSERT INTO `user` (tenant_id, phone, real_name, role, points, password, create_time)
VALUES (1, '13800138000', '测试村民', 'VILLAGER', 100, '$2a$10$NkMpL7K9JqXyZ...', NOW());
```

然后使用 Postman 请求：

```http
POST /api/auth/login
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "123456"
}
```

返回 token 后，在其他接口的 Header 中添加：

```
Authorization: Bearer <token>
```

---

## 配置说明

所有配置集中在 `application-dev.yml` 和 `application-prod.yml` 中。关键配置项：

| 配置项                       | 说明                             | 示例值                                                |
| ---------------------------- | -------------------------------- | ----------------------------------------------------- |
| `spring.datasource.url`      | 数据库连接地址                   | `jdbc:mysql://localhost:3306/village_db?useSSL=false` |
| `spring.datasource.username` | 数据库用户名                     | `root`                                                |
| `spring.datasource.password` | 数据库密码                       | `123456`                                              |
| `jwt.secret`                 | JWT 签名密钥（生产环境务必修改） | `villageSecretKey2026SecureEnough123!@#`              |
| `jwt.expiration`             | 令牌有效期（毫秒）               | `604800000` (7天)                                     |
| `aliyun.oss.endpoint`        | OSS 区域节点                     | `oss-cn-shenzhen.aliyuncs.com`                        |
| `aliyun.oss.access-key-id`   | 阿里云 AccessKey ID              | `LTAI5t...`                                           |
| `aliyun.oss.bucket-name`     | 存储桶名称                       | `village-images`                                      |
| `warning.no-apply-days`      | 无申报预警天数                   | `14`                                                  |
| `server.port`                | 服务端口                         | `8080`                                                |

**生产环境建议**：使用环境变量覆盖敏感配置，例如：

```bash
java -jar village-governance.jar --spring.profiles.active=prod \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET}
```

---

## API 文档与示例

所有 API 遵循 RESTful 风格，统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1. 认证

**POST /api/auth/login**  
请求体：

```json
{
  "phone": "13800138000",
  "password": "123456"
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlZJTExBR0VSIiwidGVuYW50SWQiOjF9.xxx"
}
```

### 2. 积分申报

**POST /api/points/apply**（需要认证，村民角色）  
请求体：

```json
{
  "ruleId": 1,
  "description": "参加村道清扫志愿活动",
  "images": "https://oss.com/1.jpg,https://oss.com/2.jpg"
}
```

响应：`{ "code": 200, "message": "success", "data": null }`

### 3. 审核申报（村委/网格员）

**PUT /api/points/apply/{applyId}/audit?approved=true&remark=通过**  

响应：`{ "code": 200, "message": "success", "data": null }`

### 4. 查询我的积分

**GET /api/points/myPoints**（需要认证）  
响应：

```json
{
  "code": 200,
  "data": 1250
}
```

### 5. 积分流水明细

**GET /api/points/flow?page=1&size=10**  
响应：

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "changeAmount": 10,
        "sourceType": "apply",
        "remark": "积分申报审核通过:参加村道清扫",
        "createTime": "2026-06-09 10:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

### 6. 积分超市

**GET /api/shop/products**  
响应：商品列表，包含 `id`, `name`, `pointsNeeded`, `stock`, `imageUrl`。

**POST /api/shop/exchange/{productId}**（需要认证）  
响应：

```json
{
  "code": 200,
  "data": {
    "exchangeCode": "12345678",
    "expireTime": "2026-06-16 10:30:00"
  }
}
```

**POST /api/shop/verify?code=12345678**（需要管理员/网格员角色）  
核销成功返回 `{ "code": 200, "message": "success" }`。

### 7. 村务通知

**GET /api/notice/list?page=1&size=10**  
响应：分页通知列表，按置顶和时间倒序。

**POST /api/notice/publish**（村委角色）  
请求体：

```json
{
  "title": "端午节活动通知",
  "content": "6月10日村广场包粽子比赛",
  "isTop": 1
}
```

### 8. 政策推送

**GET /api/policy/list?category=医保**  
**POST /api/policy/publish**（村委角色）

详细接口定义请访问 `http://localhost:8080/doc.html`，支持在线调试。

---

## 部署指南（生产环境）

### 方式一：直接运行 jar 包

1. 打包：`mvn clean package`
2. 将 `target/village-governance-1.0.0.jar` 上传到服务器
3. 创建 `application-prod.yml` 并配置生产数据库、JWT 密钥等
4. 运行：

```bash
java -jar village-governance-1.0.0.jar --spring.profiles.active=prod
```

建议使用 systemd 或 supervisor 管理进程，防止意外退出。

### 方式二：Docker 容器化（推荐）

项目根目录已提供 `Dockerfile` 和 `docker-compose.yml`。

**Dockerfile**：

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/village-governance-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

**docker-compose.yml**：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: village_db
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docs/schema.sql:/docker-entrypoint-initdb.d/init.sql
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/village_db?useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 123456
      JWT_SECRET: your_production_secret
volumes:
  mysql-data:
```

一键启动：

```bash
docker-compose up -d
```

### 监控与日志

- **健康检查**：`http://your-server:8080/actuator/health`
- **日志文件**：`./logs/village.log`（按天滚动，保留30天）
- **预警日志**：数据库 `warning_log` 表记录自动生成的预警信息

---

## 项目结构

```
src/main/java/com/scau/village/
├── VillageApplication.java                     # Spring Boot 启动类
├── common/                                     # 通用组件
│   ├── config/                                 # 配置类（多租户、安全、拦截器、跨域）
│   │   ├── CorsConfig.java
│   │   ├── MybatisPlusConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── PasswordEncoderConfig.java
│   ├── context/                                # 用户上下文（ThreadLocal）
│   │   └── UserContext.java
│   ├── exception/                              # 全局异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── interceptor/                            # JWT 拦截器
│   │   └── JwtInterceptor.java
│   ├── result/                                 # 统一返回对象
│   │   └── Result.java
│   └── utils/                                  # 工具类
│       ├── JwtUtils.java
│       ├── OssUtils.java
│       └── DesensitizationUtils.java
├── module/                                     # 业务模块（按功能分包）
│   ├── auth/                                   # 认证模块
│   │   ├── controller/AuthController.java
│   │   └── dto/LoginDto.java
│   ├── user/                                   # 用户管理
│   │   ├── entity/User.java
│   │   ├── mapper/UserMapper.java
│   │   ├── service/UserService.java
│   │   └── service/impl/UserServiceImpl.java
│   ├── tenant/                                 # 多租户管理
│   │   ├── entity/Tenant.java
│   │   ├── mapper/TenantMapper.java
│   │   └── service/...
│   ├── points/                                 # 积分模块（规则、申报、流水）
│   │   ├── controller/PointsController.java
│   │   ├── dto/ApplyDto.java
│   │   ├── entity/{PointsRule,PointsApply,PointsFlow}.java
│   │   ├── mapper/...
│   │   └── service/...
│   ├── shop/                                   # 积分超市（商品、兑换、核销）
│   │   ├── controller/ShopController.java
│   │   ├── entity/{Product,ExchangeRecord}.java
│   │   ├── mapper/...
│   │   └── service/...
│   ├── activity/                               # 活动管理
│   ├── notice/                                 # 村务通知
│   ├── policy/                                 # 政策推送
│   ├── warning/                                # 预警中心（定时任务+日志）
│   └── village/                                # 村务统计（仪表盘）
├── resources/
│   ├── application.yml                         # 主配置
│   ├── application-dev.yml                     # 开发环境配置
│   ├── application-prod.yml                    # 生产环境配置（需自行创建）
│   └── logback-spring.xml                      # 日志滚动配置
└── test/                                       # 单元测试
    └── java/com/scau/village/modules/points/PointsServiceTest.java
```

---

## 开发规范

### 代码风格

- 使用 **Lombok** 减少 getter/setter，实体类加 `@Data`。
- 包名全小写，类名大驼峰，方法名小驼峰，常量全大写下划线。
- Controller 只做参数校验、调用 Service、返回 Result，不写业务逻辑。
- Service 层必须使用 `@Transactional` 保证数据一致性（查询除外）。
- 所有接口返回统一 `Result` 对象。

### 数据库规范

- 表名小写，下划线分隔（如 `points_rule`）。
- 每张表必须有 `id` 自增主键，以及 `tenant_id`（租户字段）。
- 逻辑删除字段 `deleted`（MyBatis-Plus 自动支持）。
- 创建时间 `create_time`，更新时间 `update_time`（可选）。
- 关键字段加索引，如 `tenant_id`, `user_id`, `status`。

### 安全要求

- 所有涉及用户隐私的接口必须认证（除登录、注册外）。
- 密码使用 **BCrypt** 加密存储，禁止明文。
- 敏感数据（手机号、身份证）在返回前端时脱敏。
- JWT 密钥不能硬编码在代码中，使用配置文件或环境变量。
- SQL 注入：使用 MyBatis-Plus 条件构造器，禁止字符串拼接。

### Git 提交规范

- 提交信息格式：`<type>(<scope>): <subject>`
  - `feat`: 新功能
  - `fix`: 修复 bug
  - `docs`: 文档更新
  - `style`: 代码格式（不影响功能）
  - `refactor`: 重构
  - `test`: 测试相关
  - `chore`: 构建/工具变动
- 示例：`feat(points): 增加每日申报次数限制`

### 测试要求

- 核心 Service（积分申报、兑换）必须编写单元测试，覆盖正常流程和边界情况。
- 使用 `@SpringBootTest` 和 `@Transactional`（测试后自动回滚）。

---

## 常见问题

**Q1: 启动时提示 `Access denied for user 'root'@'localhost'`**  
A: 检查 `application-dev.yml` 中的数据库用户名和密码是否正确，确保 MySQL 服务已启动且允许连接。

**Q2: 积分兑换时提示 `商品库存已被其他用户抢先`**  
A: 这是乐观锁冲突导致的正常提示，说明并发较高，重试即可。前端可做自动重试（最多3次）。

**Q3: 如何生成 BCrypt 加密后的测试密码？**  
A: 临时在 `VillageApplication` 中添加以下代码，运行一次后删除：

```java
@Bean
CommandLineRunner temp(BCryptPasswordEncoder encoder) {
    return args -> System.out.println(encoder.encode("123456"));
}
```

**Q4: 接口文档 `/doc.html` 无法访问？**  
A: 检查是否添加了 Knife4j 依赖，以及 `WebConfig` 中是否排除了 `/doc.html` 路径。默认已排除。

**Q5: 多租户如何实现数据隔离？**  
A: 登录时 JWT 中包含 `tenantId`，拦截器解析后存入 `UserContext`，MyBatis-Plus 多租户插件自动为所有 SQL 添加 `tenant_id = ?` 条件。超级管理员角色可查看所有租户数据。

**Q6: 如何添加新的积分规则模板？**  
A: 在 `PointsRuleController` 中调用 `importTemplate` 接口，传入模板编号（如 `PARTY_BUILDING`），后端从预定义 JSON 读取规则并批量插入。

**Q7: 生产环境如何修改 JWT 密钥？**  
A: 使用环境变量 `JWT_SECRET` 覆盖配置，不要在配置文件中提交真实密钥。

**Q8: 村民端 H5 如何调试？**  
A: 后端启动后，直接访问 `http://localhost:8080`（需前端静态资源），或者单独运行前端项目调用后端 API。

**Q9: 定时任务预警不生效？**  
A: 检查 `@EnableScheduling` 是否在启动类上，以及 `cron` 表达式是否正确。日志中会打印 `执行预警检查...` 字样。

**Q10: 如何批量导入村民档案？**  
A: 在管理后台的“村民档案”模块，提供 Excel 导入功能（需前端实现），后端已预留接口。

---

## 附录

### 完整建表 SQL（village_db）

```sql
-- 租户表
CREATE TABLE tenant (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_code VARCHAR(32) UNIQUE NOT NULL,
    tenant_name VARCHAR(64) NOT NULL,
    logo_url VARCHAR(255),
    contact_person VARCHAR(32),
    contact_phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    create_time DATETIME
);

-- 用户表
CREATE TABLE `user` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    openid VARCHAR(64),
    phone VARCHAR(11),
    real_name VARCHAR(32),
    id_card VARCHAR(18),
    village_group VARCHAR(32),
    is_party_member TINYINT DEFAULT 0,
    role VARCHAR(20),
    points INT DEFAULT 0,
    password VARCHAR(100),
    create_time DATETIME,
    deleted TINYINT DEFAULT 0
);

-- 积分规则表
CREATE TABLE points_rule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    rule_name VARCHAR(64),
    category VARCHAR(32),
    points INT,
    audit_flow VARCHAR(10),
    need_photo TINYINT DEFAULT 0,
    max_times_per_day INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    sort_order INT,
    create_time DATETIME
);

-- 积分申报表
CREATE TABLE points_apply (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    user_id INT,
    rule_id INT,
    description VARCHAR(255),
    images VARCHAR(500),
    status VARCHAR(20),
    auditor_id INT,
    audit_remark VARCHAR(255),
    audit_time DATETIME,
    create_time DATETIME
);

-- 积分流水表
CREATE TABLE points_flow (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    change_amount INT,
    source_type VARCHAR(20),
    source_id INT,
    remark VARCHAR(255),
    create_time DATETIME
);

-- 商品表（添加 version 字段用于乐观锁）
CREATE TABLE product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    name VARCHAR(64),
    points_needed INT,
    stock INT,
    image_url VARCHAR(255),
    description TEXT,
    status TINYINT DEFAULT 1,
    version INT DEFAULT 0,
    create_time DATETIME
);

-- 兑换记录表
CREATE TABLE exchange_record (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    user_id INT,
    product_id INT,
    exchange_code VARCHAR(32),
    status VARCHAR(20),
    used_time DATETIME,
    expire_time DATETIME,
    create_time DATETIME
);

-- 活动表
CREATE TABLE activity (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    title VARCHAR(128),
    start_time DATETIME,
    end_time DATETIME,
    location VARCHAR(255),
    reward_points INT,
    description TEXT,
    max_participants INT,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);

-- 活动报名表
CREATE TABLE activity_registration (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    activity_id INT,
    user_id INT,
    participant_name VARCHAR(32),
    phone VARCHAR(11),
    remark VARCHAR(255),
    signed_in TINYINT DEFAULT 0,
    sign_time DATETIME,
    create_time DATETIME
);

-- 村务通知表
CREATE TABLE notice (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    title VARCHAR(128),
    content TEXT,
    images VARCHAR(500),
    is_top TINYINT DEFAULT 0,
    read_count INT DEFAULT 0,
    create_time DATETIME
);

-- 政策推送表
CREATE TABLE policy (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    category VARCHAR(32),
    title VARCHAR(128),
    content TEXT,
    attachment_url VARCHAR(255),
    effective_time DATETIME,
    create_time DATETIME
);

-- 预警日志表
CREATE TABLE warning_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL,
    warning_type VARCHAR(32),
    warning_content VARCHAR(255),
    occurred_at DATETIME,
    is_resolved TINYINT DEFAULT 0,
    resolved_at DATETIME
);

-- 操作日志表
CREATE TABLE operation_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    operation_type VARCHAR(32),
    content VARCHAR(500),
    create_time DATETIME
);
```

### 环境变量参考（生产环境）

```bash
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret
export OSS_ACCESS_KEY=your_oss_key
export OSS_ACCESS_SECRET=your_oss_secret
```

---

**文档版本**：1.0.0  
**最后更新**：2026-06-09  
**维护者**：华南农业大学 云耕科创实践队  
**联系方式**：963032295@qq.com  

**祝你开发顺利，项目完美交付！**