package com.onepiece.infrastructure.dao.po;

import com.onepiece.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * AI客户端模型配置表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiClientModelConfig extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 创建时间
     */
    private Date createTime;

}