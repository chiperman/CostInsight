# Project: 投资记录管理系统（后端）

## 技术栈

### 基础框架

- Java 17（语言版本）

- Spring Boot 3.x（基础开发框架）

- Spring Cloud 2023.x（微服务框架）

- Spring Cloud Alibaba（阿里巴巴微服务生态）

### 微服务与中间件

- Nacos（服务注册与配置中心）

- Gateway（Spring Cloud Gateway 作为 API 网关）

- RocketMQ（消息队列，用于异步处理和事件通知）

- Seata（分布式事务解决方案）

- OpenFeign（服务间调用）

### 持久层

- MyBatis-Plus（ORM 框架，简化 CRUD）

- MySQL 8.x（关系型数据库）

- Druid（数据库连接池）

- Redis

### 安全认证

- JWT + 自己实现拦截器（用户认证与权限管理）

### 开发辅助

- Lombok（简化实体类编写）

- MapStruct（对象映射）

- Swagger/OpenAPI 3（接口文档）

- Maven（项目管理）

- Logback/Slf4j（日志管理）

## 系统需求

### 用户管理模块

- 用户注册（用户名、邮箱、密码）
- 用户登录（JWT 生成与验证）
- 修改用户信息（昵称、头像、邮箱、投资偏好）
- 修改密码
- 删除用户（软删除）

### 投资项目管理

- 新增投资项目（名称、类型、风险等级、备注）
- 修改投资项目
- 删除投资项目
- 查询投资项目（分页、模糊搜索、按类型筛选）

### 定投记录管理

- 新增定投记录（关联项目、金额、日期、备注）
- 修改定投记录
- 删除定投记录
- 查询定投记录（分页、按日期范围、按项目筛选）

### 收益与统计

- 计算投资项目的平均成本
- 计算当前收益（根据投入与当前市值）
- 投资分布统计（饼图 API 数据）
- 收益变化趋势（折线图 API 数据）

### 定投分配

- 自动按比例分配定投金额到各项目
- 记录分配计划与执行金额

## 数据库设计

表：

1. `user` - 用户信息
2. `investment_project` - 投资项目
3. `investment_record` - 定投记录
4. `allocation_plan` - 定投分配计划
5. `project_value`（可选）- 历史行情记录

## API 规范

所有接口返回统一 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

示例接口：

```
POST /api/auth/register - 注册
POST /api/auth/login - 登录
GET /api/user/me - 获取当前用户信息
PUT /api/user/update - 更新用户信息
POST /api/projects - 新增投资项目
GET /api/projects - 查询投资项目列表
POST /api/investments - 新增定投记录
GET /api/investments - 查询定投记录
GET /api/statistics/portfolio - 获取投资组合占比
GET /api/statistics/trend - 获取收益趋势
```

## 非功能性需求

所有密码必须加密存储（BCrypt）
所有敏感接口必须使用 JWT 鉴权
支持跨域（CORS）
日志记录（操作日志、异常日志）

## 项目目录结构

```
investment-tracker/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/investmenttracker/
│   │   │   ├── config/           # Spring Security、JWT、CORS 配置
│   │   │   ├── controller/       # 控制层
│   │   │   ├── dto/               # 数据传输对象
│   │   │   ├── entity/            # JPA 实体类
│   │   │   ├── exception/         # 全局异常处理
│   │   │   ├── repository/        # JPA 仓库接口
│   │   │   ├── service/           # 业务逻辑接口
│   │   │   ├── service/impl/      # 业务逻辑实现
│   │   │   ├── util/              # 工具类（JWT、加密等）
│   │   │   └── InvestmentTrackerApplication.java
│   │   └── resources/
│   │       ├── application.yml    # 配置文件（数据库、JWT、端口等）
│   │       ├── schema.sql         # 数据库建表脚本
│   │       └── data.sql           # 初始化数据（可选）
│   └── test/java/com/example/investmenttracker/
│       ├── controller/            # 控制层单元测试
│       ├── service/               # 服务层单元测试
│       └── repository/            # 仓库层单元测试
│
├── pom.xml
└── README.md
```

## 任务

初始化 Spring Boot 项目，配置 MySQL
实现用户注册、登录、鉴权功能
实现投资项目 CRUD
实现定投记录 CRUD
实现收益与分配计算逻辑
编写单元测试（JUnit）

## 要求

1. 根据每个模块的 schema.sql 文件创建相应的实体类和仓库接口
2. 关键业务逻辑实现后必须有单元测试覆盖，然后解决问题

## 沟通语气

- **教学导向**: 解释为什么这样做，不只是怎样做。
- **实用主义**: 提供可直接使用的解决方案。
- **简洁明了**: 避免冗长的解释。

## 💡 常见任务示例

### 示例1：添加一个新的 API 端点

**用户问题**: "我需要为 'products' 创建一个 GET /api/v1/products 的端点，用来获取所有产品列表。"
**期望回答**: (在这里描述你期望 AI 如何回答，包括代码结构、文件位置等)

## GIT

提交代码的时候，请参考 Conventional Commits，同时参考之前的提交记录，避免重复，避免使用中文描述，请使用英文描述，避免使用特殊字符，避免使用
emoji，避免使用空格，简约精炼
commit message，尽量不添加 content 除非有多个改动。如果改动在不同的功能，请使用不同的 commit message。