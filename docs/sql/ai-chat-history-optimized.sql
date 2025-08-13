# AI Agent 聊天历史表 - 优化版本
# 支持大消息内容的分块存储和压缩
# ************************************************************

USE `ai-agent-station`;

# 聊天会话表（保持不变）
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

# 聊天消息表 - 优化版本
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_chat_message`;

CREATE TABLE `ai_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息唯一标识（前端生成）',
  `role` varchar(20) NOT NULL COMMENT '角色(user/assistant)',
  `content` text COMMENT '消息内容（小于64KB的内容直接存储）',
  `content_size` bigint(20) DEFAULT '0' COMMENT '内容总大小（字节）',
  `is_large_content` tinyint(1) DEFAULT '0' COMMENT '是否为大内容（需要分块存储）',
  `is_compressed` tinyint(1) DEFAULT '0' COMMENT '是否已压缩',
  `content_hash` varchar(64) DEFAULT NULL COMMENT '内容哈希值（用于去重）',
  `message_order` int(11) NOT NULL COMMENT '消息顺序',
  `is_streaming` tinyint(1) DEFAULT '0' COMMENT '是否为流式消息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_message_order` (`message_order`),
  KEY `idx_content_hash` (`content_hash`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息表（优化版）';

# 消息内容块表
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_chat_message_chunk`;

CREATE TABLE `ai_chat_message_chunk` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '块ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `chunk_index` int(11) NOT NULL COMMENT '块索引（从0开始）',
  `chunk_data` longblob NOT NULL COMMENT '块数据（可能经过压缩）',
  `chunk_size` int(11) NOT NULL COMMENT '块大小（字节）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_chunk` (`message_id`, `chunk_index`),
  KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息内容块表';

# 消息内容缓存表（可选）
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ai_chat_message_cache`;

CREATE TABLE `ai_chat_message_cache` (
  `content_hash` varchar(64) NOT NULL COMMENT '内容哈希值',
  `compressed_content` longblob NOT NULL COMMENT '压缩后的内容',
  `original_size` bigint(20) NOT NULL COMMENT '原始大小',
  `compressed_size` bigint(20) NOT NULL COMMENT '压缩后大小',
  `reference_count` int(11) DEFAULT '1' COMMENT '引用计数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_access_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间',
  PRIMARY KEY (`content_hash`),
  KEY `idx_last_access` (`last_access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息内容缓存表（去重存储）';
