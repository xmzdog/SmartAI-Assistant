package com.onepiece.domain.agent.service;

/**
 * 大内容处理服务接口
 * 处理超大消息内容的存储和检索
 * 
 * @author xmz
 * @date 2025-08-13
 */
public interface ILargeContentService {

    /**
     * 内容大小阈值（64KB）
     */
    int CONTENT_SIZE_THRESHOLD = 64 * 1024;

    /**
     * 块大小（1MB）
     */
    int CHUNK_SIZE = 1024 * 1024;

    /**
     * 存储大内容
     * @param messageId 消息ID
     * @param content 内容
     * @return 内容哈希值
     */
    String storeLargeContent(String messageId, String content);

    /**
     * 检索大内容
     * @param messageId 消息ID
     * @return 完整内容
     */
    String retrieveLargeContent(String messageId);

    /**
     * 删除大内容
     * @param messageId 消息ID
     */
    void deleteLargeContent(String messageId);

    /**
     * 压缩内容
     * @param content 原始内容
     * @return 压缩后的字节数组
     */
    byte[] compressContent(String content);

    /**
     * 解压内容
     * @param compressedData 压缩数据
     * @return 原始内容
     */
    String decompressContent(byte[] compressedData);

    /**
     * 计算内容哈希
     * @param content 内容
     * @return SHA-256哈希值
     */
    String calculateContentHash(String content);

    /**
     * 检查内容是否需要分块存储
     * @param content 内容
     * @return 是否需要分块
     */
    default boolean isLargeContent(String content) {
        return content != null && content.getBytes().length > CONTENT_SIZE_THRESHOLD;
    }

}
