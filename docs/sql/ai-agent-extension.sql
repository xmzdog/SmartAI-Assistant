# AI Agent 系统扩展表
# 补充任务管理和系统监控相关表
# ************************************************************

USE `ai-agent-station`;

# 任务执行记录表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `task_execution_record`;

CREATE TABLE `task_execution_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID（唯一标识）',
  `agent_id` bigint(20) DEFAULT NULL COMMENT '执行的智能体ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `task_description` text COMMENT '任务描述',
  `task_type` varchar(50) DEFAULT NULL COMMENT '任务类型(MANUS/AGENT/RAG等)',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态(PENDING/RUNNING/COMPLETED/FAILED/CANCELLED)',
  `progress` int(11) DEFAULT '0' COMMENT '执行进度(0-100)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `execution_time` bigint(20) DEFAULT NULL COMMENT '执行时间(毫秒)',
  `result` text COMMENT '执行结果',
  `error_message` text COMMENT '错误信息',
  `input_params` text COMMENT '输入参数(JSON格式)',
  `output_data` text COMMENT '输出数据(JSON格式)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

# 任务执行日志表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `task_execution_log`;

CREATE TABLE `task_execution_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_record_id` bigint(20) NOT NULL COMMENT '任务记录ID',
  `step_name` varchar(100) DEFAULT NULL COMMENT '步骤名称',
  `step_description` varchar(500) DEFAULT NULL COMMENT '步骤描述',
  `step_type` varchar(50) DEFAULT NULL COMMENT '步骤类型(THINKING/ACTION/OBSERVATION/TOOL_CALL等)',
  `step_status` varchar(20) DEFAULT NULL COMMENT '步骤状态(SUCCESS/FAILED/RUNNING)',
  `step_order` int(11) DEFAULT NULL COMMENT '步骤顺序',
  `input_data` text COMMENT '输入数据',
  `output_data` text COMMENT '输出数据',
  `error_message` text COMMENT '错误信息',
  `execution_time` bigint(20) DEFAULT NULL COMMENT '步骤执行时间(毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_record_id` (`task_record_id`),
  KEY `idx_step_order` (`step_order`),
  CONSTRAINT `fk_task_execution_log_record` FOREIGN KEY (`task_record_id`) REFERENCES `task_execution_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

# 系统监控数据表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `system_monitor_data`;

CREATE TABLE `system_monitor_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_time` datetime NOT NULL COMMENT '监控时间',
  `cpu_usage` decimal(5,2) DEFAULT NULL COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) DEFAULT NULL COMMENT '内存使用率(%)',
  `disk_usage` decimal(5,2) DEFAULT NULL COMMENT '磁盘使用率(%)',
  `active_tasks` int(11) DEFAULT '0' COMMENT '活跃任务数',
  `completed_tasks_today` int(11) DEFAULT '0' COMMENT '今日完成任务数',
  `failed_tasks_today` int(11) DEFAULT '0' COMMENT '今日失败任务数',
  `average_response_time` decimal(10,2) DEFAULT NULL COMMENT '平均响应时间(毫秒)',
  `system_load` decimal(5,2) DEFAULT NULL COMMENT '系统负载(0-1)',
  `agent_status` varchar(20) DEFAULT 'IDLE' COMMENT 'Agent状态(IDLE/BUSY/ERROR)',
  `uptime_seconds` bigint(20) DEFAULT NULL COMMENT '运行时间(秒)',
  `last_activity` datetime DEFAULT NULL COMMENT '最后活动时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_monitor_time` (`monitor_time`),
  KEY `idx_agent_status` (`agent_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统监控数据表';

# 知识库文档记录表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_rag_document`;

CREATE TABLE `ai_rag_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rag_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `document_name` varchar(255) NOT NULL COMMENT '文档名称',
  `document_path` varchar(500) DEFAULT NULL COMMENT '文档路径',
  `document_type` varchar(50) DEFAULT NULL COMMENT '文档类型(PDF/TXT/MD等)',
  `document_size` bigint(20) DEFAULT NULL COMMENT '文档大小(字节)',
  `chunk_count` int(11) DEFAULT '0' COMMENT '分块数量',
  `embedding_status` varchar(20) DEFAULT 'PENDING' COMMENT '向量化状态(PENDING/PROCESSING/COMPLETED/FAILED)',
  `upload_time` datetime DEFAULT NULL COMMENT '上传时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rag_id` (`rag_id`),
  KEY `idx_embedding_status` (`embedding_status`),
  CONSTRAINT `fk_rag_document_rag` FOREIGN KEY (`rag_id`) REFERENCES `ai_rag_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档记录表';

# 智能体配置快照表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_agent_config_snapshot`;

CREATE TABLE `ai_agent_config_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体ID',
  `snapshot_name` varchar(100) NOT NULL COMMENT '快照名称',
  `config_data` longtext NOT NULL COMMENT '配置数据(JSON格式)',
  `version` varchar(20) DEFAULT '1.0' COMMENT '版本号',
  `is_active` tinyint(1) DEFAULT '0' COMMENT '是否为当前激活配置',
  `description` varchar(500) DEFAULT NULL COMMENT '快照描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `fk_config_snapshot_agent` FOREIGN KEY (`agent_id`) REFERENCES `ai_agent` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体配置快照表';

# 初始化一些监控数据
INSERT INTO `system_monitor_data` (`monitor_time`, `cpu_usage`, `memory_usage`, `disk_usage`, `active_tasks`, `completed_tasks_today`, `failed_tasks_today`, `average_response_time`, `system_load`, `agent_status`, `uptime_seconds`, `last_activity`)
VALUES 
  (NOW(), 25.5, 68.2, 45.8, 2, 156, 8, 1250.5, 0.45, 'IDLE', 3600, NOW()),
  (DATE_SUB(NOW(), INTERVAL 5 MINUTE), 30.2, 65.8, 45.8, 3, 155, 8, 1180.2, 0.52, 'BUSY', 3300, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
  (DATE_SUB(NOW(), INTERVAL 10 MINUTE), 28.7, 70.1, 45.8, 1, 154, 7, 1320.8, 0.48, 'IDLE', 3000, DATE_SUB(NOW(), INTERVAL 7 MINUTE));


