package com.ruoyi.bussiness.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * defi订单对象 t_defi_order
 *
 * @author ruoyi
 * @date 2023-08-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_defi_order")
public class DefiOrderDTO extends BaseEntity {

private static final long serialVersionUID=1L;

    /**
     * id
     */
        @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 收益金额
     */
    private BigDecimal amount;
    /**
     * 钱包金额
     */
    private BigDecimal totleAmount;
    /**
     * 收益率
     */
    private BigDecimal rate;
    /**
     * $column.columnComment
     */
    private String searchValue;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 代理ids
     */
    private String adminParentIds;
    private Long createTimes;
}
