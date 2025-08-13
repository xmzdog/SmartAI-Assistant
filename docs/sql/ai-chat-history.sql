# AI Agent 聊天历史表
# 用于存储用户与智能体的对话记录
# ************************************************************

USE `ai-agent-station`;

# 聊天会话表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_chat_session`;

CREATE TABLE `ai_chat_session` (
  `id` varchar(64) NOT NULL COMMENT '会话ID（前端生成的唯一标识）',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体ID',
  `rag_id` bigint(20) DEFAULT NULL COMMENT '知识库ID',
  `session_title` varchar(200) DEFAULT NULL COMMENT '会话标题',
  `message_count` int(11) DEFAULT '0' COMMENT '消息数量',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';

# 聊天消息表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_chat_message`;

CREATE TABLE `ai_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息唯一标识（前端生成）',
  `role` varchar(20) NOT NULL COMMENT '角色(user/assistant)',
  `content` longtext NOT NULL COMMENT '消息内容',
  `message_order` int(11) NOT NULL COMMENT '消息顺序',
  `is_streaming` tinyint(1) DEFAULT '0' COMMENT '是否为流式消息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_message_order` (`message_order`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表';
