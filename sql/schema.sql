CREATE DATABASE IF NOT EXISTS ticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ticket_db;

-- 1. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(500) COMMENT '角色描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_role_code (role_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色表';

-- 2. SLA规则表
CREATE TABLE IF NOT EXISTS sla_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SLA规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    priority_level INT NOT NULL COMMENT '优先级级别',
    response_time INT NOT NULL COMMENT '响应时限（分钟）',
    resolve_time INT NOT NULL COMMENT '解决时限（分钟）',
    description VARCHAR(500) COMMENT '规则描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_priority_level (priority_level),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='SLA规则表';

-- 3. 工单类型表
CREATE TABLE IF NOT EXISTS ticket_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单类型ID',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
    description VARCHAR(500) COMMENT '类型描述',
    default_sla_id BIGINT COMMENT '默认SLA规则ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_type_code (type_code),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_type_sla FOREIGN KEY (default_sla_id) REFERENCES sla_rule(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单类型表';

-- 4. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

-- 5. 工单主表
CREATE TABLE IF NOT EXISTS ticket_main (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    ticket_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单号',
    type_id BIGINT NOT NULL COMMENT '工单类型ID',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '工单描述',
    reporter_id BIGINT NOT NULL COMMENT '创建人ID',
    assignee_id BIGINT COMMENT '处理人ID',
    sla_rule_id BIGINT NOT NULL COMMENT 'SLA规则ID',
    urgency INT NOT NULL DEFAULT 1 COMMENT '紧急程度 1-低 2-中 3-高',
    impact_scope INT NOT NULL DEFAULT 1 COMMENT '影响范围 1-个人 2-部门 3-全公司',
    priority INT NOT NULL DEFAULT 1 COMMENT '综合优先级',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '工单状态',
    sla_start_time DATETIME COMMENT 'SLA开始时间',
    sla_pause_time DATETIME COMMENT 'SLA暂停时间',
    sla_pause_duration BIGINT DEFAULT 0 COMMENT 'SLA暂停时长（毫秒）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    resolved_at DATETIME COMMENT '解决时间',
    closed_at DATETIME COMMENT '关闭时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    original_ticket_id BIGINT COMMENT '原始工单ID（重开工单时使用）',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_ticket_no (ticket_no),
    INDEX idx_type_id (type_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_deleted (deleted),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_ticket_type FOREIGN KEY (type_id) REFERENCES ticket_type(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_sla FOREIGN KEY (sla_rule_id) REFERENCES sla_rule(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_original FOREIGN KEY (original_ticket_id) REFERENCES ticket_main(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单主表';

-- 6. 工单附件表
CREATE TABLE IF NOT EXISTS ticket_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_deleted (deleted),
    CONSTRAINT fk_attachment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单附件表';

-- 7. 工单流程记录表
CREATE TABLE IF NOT EXISTS ticket_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流程记录ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    from_status VARCHAR(20) COMMENT '原状态',
    to_status VARCHAR(20) NOT NULL COMMENT '目标状态',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark VARCHAR(1000) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_process_ticket FOREIGN KEY (ticket_id) REFERENCES ticket_main(id) ON DELETE CASCADE,
    CONSTRAINT fk_process_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单流程记录表';

-- 8. 工单评价表
CREATE TABLE IF NOT EXISTS ticket_evaluate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    ticket_id BIGINT NOT NULL UNIQUE COMMENT '工单ID',
    score INT COMMENT '评分（1-5星）',
    comment VARCHAR(1000) COMMENT '评价内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    INDEX idx_ticket_id (ticket_id),
    CONSTRAINT fk_evaluate_ticket FOREIGN KEY (ticket_id) REFERENCES ticket_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单评价表';

-- 9. 工单审计日志表
CREATE TABLE IF NOT EXISTS ticket_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    ticket_id BIGINT COMMENT '工单ID',
    ticket_no VARCHAR(50) COMMENT '工单号',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_detail VARCHAR(2000) COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT '操作IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单审计日志表';

-- 10. 工单分配策略表
CREATE TABLE IF NOT EXISTS assign_strategy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分配策略ID',
    strategy_code VARCHAR(50) NOT NULL UNIQUE COMMENT '策略编码',
    strategy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    description VARCHAR(500) COMMENT '策略描述',
    is_active TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_strategy_code (strategy_code),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单分配策略表';

-- ===================== 初始化数据 =====================
INSERT INTO sys_role (role_code, role_name, description) VALUES 
('EMPLOYEE','普通员工','提交工单、查看本人工单、取消待分配工单、驳回待确认工单、确认关闭并完成服务评价'),
('IT_SUPPORT','IT支持人员','认领待分配工单、处理工单、挂起/恢复工单、转交工单、添加协作者、查看本人负责工单、接收SLA超时提醒'),
('SUPERVISOR','服务台主管','手工分配工单、配置自动分配策略、监控SLA执行、查看团队报表、处理超时升级工单'),
('ADMIN','系统管理员','用户与角色管理、系统参数配置、查看全量工单、审计日志查询、可强制关闭工单');

INSERT INTO sla_rule (rule_name, priority_level, response_time, resolve_time, description) VALUES 
('低优先级',1,30,480,'响应时限30分钟，解决时限8小时'),
('中优先级',2,15,240,'响应时限15分钟，解决时限4小时'),
('高优先级',3,5,60,'响应时限5分钟，解决时限1小时');

INSERT INTO ticket_type (type_code, type_name, description, default_sla_id) VALUES 
('HARDWARE','硬件故障','电脑、打印机等硬件设备故障',2),
('SOFTWARE','软件问题','操作系统、办公软件等软件问题',2),
('NETWORK','网络问题','网络连接、Wi-Fi等网络问题',3),
('ACCOUNT','账号权限','账号创建、权限变更等问题',1),
('OTHER','其他问题','其他未分类的IT问题',1);

INSERT INTO assign_strategy (strategy_code, strategy_name, description, is_active) VALUES 
('ROUND_ROBIN','轮询分配','按顺序轮流分配给IT支持人员',1),
('LOAD_BALANCE','负载均衡','优先分配给待处理工单最少的人员',0);

INSERT INTO sys_user (username,password,real_name,email,phone,role_id) VALUES 
('admin','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','系统管理员','admin@company.com','13800138000',4),
('supervisor','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','服务台主管','supervisor@company.com','13800138001',3),
('itsupport1','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','IT支持人员1','itsupport1@company.com','13800138002',2),
('itsupport2','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','IT支持人员2','itsupport2@company.com','13800138003',2),
('employee1','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','普通员工1','employee1@company.com','13800138004',1),
('employee2','$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq','普通员工2','employee2@company.com','13800138005',1);