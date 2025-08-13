package com.onepiece.domain.agent.service.chat;

import com.onepiece.domain.agent.service.ILargeContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 大内容处理服务实现
 * 
 * @author xmz
 * @date 2025-08-13
 */
@Slf4j
@Service
public class LargeContentService implements ILargeContentService {

    // 临时内存缓存（生产环境建议使用Redis或数据库）
    private final ConcurrentHashMap<String, List<byte[]>> contentChunks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> contentHashes = new ConcurrentHashMap<>();

    @Override
    public String storeLargeContent(String messageId, String content) {
        try {
            log.info("存储大内容，消息ID: {}, 内容大小: {} 字节", messageId, content.getBytes().length);

            // 计算内容哈希
            String contentHash = calculateContentHash(content);
            contentHashes.put(messageId, contentHash);

            // 压缩内容
            byte[] compressedData = compressContent(content);
            log.info("内容压缩完成，原始大小: {}, 压缩后大小: {}, 压缩率: {:.2f}%", 
                    content.getBytes().length, compressedData.length, 
                    (1.0 - (double)compressedData.length / content.getBytes().length) * 100);

            // 分块存储
            List<byte[]> chunks = splitIntoChunks(compressedData);
            contentChunks.put(messageId, chunks);

            log.info("大内容存储完成，消息ID: {}, 分块数: {}", messageId, chunks.size());
            return contentHash;

        } catch (Exception e) {
            log.error("存储大内容失败，消息ID: {}", messageId, e);
            throw new RuntimeException("存储大内容失败", e);
        }
    }

    @Override
    public String retrieveLargeContent(String messageId) {
        try {
            log.info("检索大内容，消息ID: {}", messageId);

            List<byte[]> chunks = contentChunks.get(messageId);
            if (chunks == null || chunks.isEmpty()) {
                log.warn("未找到大内容，消息ID: {}", messageId);
                return null;
            }

            // 合并分块
            byte[] compressedData = mergeChunks(chunks);

            // 解压内容
            String content = decompressContent(compressedData);

            log.info("大内容检索完成，消息ID: {}, 内容大小: {} 字节", messageId, content.getBytes().length);
            return content;

        } catch (Exception e) {
            log.error("检索大内容失败，消息ID: {}", messageId, e);
            throw new RuntimeException("检索大内容失败", e);
        }
    }

    @Override
    public void deleteLargeContent(String messageId) {
        try {
            log.info("删除大内容，消息ID: {}", messageId);

            contentChunks.remove(messageId);
            contentHashes.remove(messageId);

            log.info("大内容删除完成，消息ID: {}", messageId);

        } catch (Exception e) {
            log.error("删除大内容失败，消息ID: {}", messageId, e);
            throw new RuntimeException("删除大内容失败", e);
        }
    }

    @Override
    public byte[] compressContent(String content) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(content.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("内容压缩失败", e);
            throw new RuntimeException("内容压缩失败", e);
        }
    }

    @Override
    public String decompressContent(byte[] compressedData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = gzipIn.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }
            
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("内容解压失败", e);
            throw new RuntimeException("内容解压失败", e);
        }
    }

    @Override
    public String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("计算内容哈希失败", e);
            throw new RuntimeException("计算内容哈希失败", e);
        }
    }

    /**
     * 将数据分割成块
     */
    private List<byte[]> splitIntoChunks(byte[] data) {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;
        
        while (offset < data.length) {
            int chunkSize = Math.min(CHUNK_SIZE, data.length - offset);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(data, offset, chunk, 0, chunkSize);
            chunks.add(chunk);
            offset += chunkSize;
        }
        
        return chunks;
    }

    /**
     * 合并分块数据
     */
    private byte[] mergeChunks(List<byte[]> chunks) {
        int totalSize = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] merged = new byte[totalSize];
        int offset = 0;
        
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }
        
        return merged;
    }

    /**
     * 获取内容统计信息
     */
    public void printStatistics() {
        log.info("=== 大内容服务统计 ===");
        log.info("缓存的消息数: {}", contentChunks.size());
        
        int totalChunks = contentChunks.values().stream()
                .mapToInt(List::size)
                .sum();
        log.info("总分块数: {}", totalChunks);
        
        long totalMemory = contentChunks.values().stream()
                .flatMap(List::stream)
                .mapToLong(chunk -> chunk.length)
                .sum();
        log.info("总内存使用: {} MB", totalMemory / (1024 * 1024));
    }

}
