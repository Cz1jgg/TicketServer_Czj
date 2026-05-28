# 企业IT服务工单管理系统

企业级IT服务工单管理系统后端与前端完整项目，采用前后端分离架构，支持工单全生命周期管理、SLA监控、RBAC权限控制。

## 📋 项目介绍

这是一个功能完整的企业级IT服务工单管理系统，包含：

- **后端**：Spring Boot 2.7.18 + Spring Data JPA + MySQL 8.0
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Axios
- **认证**：JWT无状态认证 + BCrypt密码加密
- **权限**：RBAC角色权限控制（四种角色）
- **特性**：工单状态机、乐观锁、SLA监控、审计日志

## 🛠 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 后端框架 |
| Java | 17 | 编程语言 |
| Spring Data JPA | 2.7.18 | 数据访问层 |
| MySQL | 8.0 | 数据库 |
| Lombok | 1.18.30 | 简化代码 |
| Springdoc OpenAPI | 1.7.0 | API文档 |
| JJWT | 0.11.5 | JWT认证 |
| Apache POI | 5.2.5 | Excel导出 |
| Hutool | 5.8.23 | 工具库 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vue Router | 4.3.x | 路由管理 |
| Pinia | 2.1.x | 状态管理 |
| Element Plus | 2.6.x | UI组件库 |
| Axios | 1.6.x | HTTP客户端 |
| Vite | 5.1.x | 构建工具 |
| ECharts | 5.5.x | 数据可视化 |
| wangEditor | 5.1.x | 富文本编辑器 |
| Dayjs | 1.11.x | 时间处理 |
| Sass | 1.69.x | 样式预处理 |

## 📁 项目结构

```
TicketServer_Czj/
├── pom.xml                              # Maven配置
├── sql/
│   └── schema.sql                       # MySQL建表脚本
├── src/main/
│   └── java/com/itticket/              # 后端源代码
│       ├── TicketManagementApplication.java
│       ├── controller/                  # 控制器层
│       ├── service/                     # 业务逻辑层
│       ├── repository/                  # 数据访问层
│       ├── entity/                      # 实体类
│       ├── dto/                         # 数据传输对象
│       ├── config/                      # 配置类
│       ├── common/                      # 通用组件
│       └── util/                        # 工具类
└── frontend/                           # 前端项目
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/                         # API接口封装
        ├── router/                      # 路由配置
        ├── stores/                      # Pinia状态管理
        ├── layout/                      # 布局组件
        ├── views/                       # 页面视图
        ├── utils/                       # 工具函数
        └── style/                       # 全局样式
```

## 🎯 功能特性

### 核心功能（P0）

- ✅ 工单创建与多条件分页查询
- ✅ 工单状态机流转控制
- ✅ 乐观锁工单认领
- ✅ 工单确认关闭与满意度评价
- ✅ JWT无状态登录认证
- ✅ RBAC角色权限控制
- ✅ 审计日志记录

### 业务功能（P1）

- ✅ 工单手工分配与自动分配策略
- ✅ 工单挂起与恢复
- ✅ 工单驳回处理
- ✅ SLA超时监控
- ✅ 基础数据报表
- ✅ 工单数据Excel批量导出

### 系统管理

- ✅ 用户管理（CRUD）
- ✅ 角色权限配置
- ✅ 工单类型配置
- ✅ SLA规则配置
- ✅ 分配策略配置

## 👥 用户角色

| 角色代码 | 角色名称 | 功能权限 |
|----------|----------|----------|
| EMPLOYEE | 普通员工 | 提交工单、查看本人工单、取消待分配工单、驳回待确认工单、确认关闭并评价 |
| IT_SUPPORT | IT支持人员 | 认领工单、处理工单、挂起/恢复工单、转交工单、添加协作者、查看负责工单 |
| SUPERVISOR | 服务台主管 | 手工分配工单、配置自动分配策略、监控SLA执行、查看团队报表、处理超时升级 |
| ADMIN | 系统管理员 | 用户与角色管理、系统参数配置、查看全量工单、审计日志查询、强制关闭工单 |

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 1. 数据库配置

创建数据库并执行建表脚本：

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE ticket_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE ticket_system;

# 执行建表脚本
SOURCE sql/schema.sql;

# 或者命令行直接执行
mysql -u root -p < sql/schema.sql
```

### 2. 后端配置

修改后端配置文件 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ticket_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

### 3. 启动后端服务

```bash
# 编译项目
mvn clean package -DskipTests

# 启动服务
mvn spring-boot:run

# 或者运行JAR包
java -jar target/ticket-management-1.0.0.jar
```

后端服务启动后运行在：http://localhost:8080

### 4. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务启动后访问：http://localhost:5173

## 📚 API文档

启动后端服务后，可通过Swagger UI访问完整的API文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin | 系统管理员 | 全部功能权限 |
| supervisor | admin | 服务台主管 | 工单分配、团队管理 |
| itsupport1 | admin | IT支持人员 | 工单处理 |
| itsupport2 | admin | IT支持人员 | 工单处理 |
| employee1 | admin | 普通员工 | 提交工单、评价 |
| employee2 | admin | 普通员工 | 提交工单、评价 |

## 📊 核心接口

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/info` | GET | 获取当前用户信息 |

### 工单接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/tickets` | GET | 查询工单列表（分页） |
| `/api/tickets` | POST | 创建新工单 |
| `/api/tickets/{id}` | GET | 获取工单详情 |
| `/api/tickets/{id}` | PUT | 更新工单 |
| `/api/tickets/{id}/claim` | POST | 认领工单 |
| `/api/tickets/{id}/process` | POST | 处理工单 |
| `/api/tickets/{id}/suspend` | POST | 挂起工单 |
| `/api/tickets/{id}/resume` | POST | 恢复工单 |
| `/api/tickets/{id}/reject` | POST | 驳回工单 |
| `/api/tickets/{id}/close` | POST | 关闭工单 |
| `/api/tickets/{id}/evaluate` | POST | 评价工单 |
| `/api/tickets/{id}/assign` | POST | 分配工单 |

### 管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/users` | GET/POST | 用户管理 |
| `/api/admin/users/{id}` | PUT/DELETE | 用户CRUD |
| `/api/admin/roles` | GET/POST | 角色管理 |
| `/api/supervisor/tickets/{id}/assign` | POST | 手工分配工单 |
| `/api/reports/daily` | GET | 日报统计 |
| `/api/reports/export` | GET | 导出工单Excel |

## 🔧 配置说明

### JWT配置

JWT密钥和过期时间在 `application.yml` 中配置：

```yaml
jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000  # 24小时
```

### 文件上传配置

```yaml
file:
  upload:
    path: ./uploads
    max-size: 10485760  # 10MB
    allowed-types: jpg,png,pdf,docx,xlsx
```

### CORS配置

允许前端开发服务器跨域访问：

```yaml
cors:
  allowed-origins: http://localhost:5173
```

## 📝 开发指南

### 代码规范

- 遵循RESTful API设计规范
- 使用统一返回格式 `Result<T>`
- 控制器层仅做参数校验和路由转发
- 业务逻辑下沉至Service层
- 使用 `@Version` 注解实现乐观锁
- 关键操作记录审计日志

### 数据库规范

- 所有表使用utf8mb4字符集
- 采用逻辑删除（deleted字段）
- 添加审计时间字段（createTime, updateTime）
- 建立必要的业务索引
- 主外键关系清晰

### 前端开发规范

- 使用Vue3组合式API
- 组件按业务模块组织
- API接口统一封装
- 样式使用SCSS预处理
- 遵循Element Plus组件规范

## 📄 License

本项目仅供学习交流使用。

## 🤝 联系方式

如有问题，请提交Issue或联系开发团队。
