package com.internal.manager.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.internal.common.annotation.Excel;
import com.internal.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 管理系统-软著管理对象SaveVO manage_soft
 *
 * @author internal
 * @date 2024-12-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ApiModel(value = "ManageSoftSaveVO",description = "管理系统-软著管理对象SaveVO")
public class ManageSoftSaveDTO extends BaseEntity
        {
private static final long serialVersionUID=1L;


    /** 软著类别[1:欣象软著,2:北光软著] */
    @ApiModelProperty(value = "软著类别[1:欣象软著,2:北光软著]")
    @NotNull(message = "类别不能为空")
    @NotEmpty(message = "类别不能为空")
    @Excel(name = "软著类别[1:欣象软著,2:北光软著]")
    private String category;

    /** 软著简称 */
    @ApiModelProperty(value = "软著简称")
    @Excel(name = "软著简称")
    private String shortName;

    /** 软著名称 */
    @ApiModelProperty(value = "软著名称")
    @Excel(name = "软著名称")
    private String name;

    /** 对应软著id */
    @ApiModelProperty(value = "对应软著id")
    @Excel(name = "对应软著id")
    private Long linkId;

    /** 版本号 */
    @ApiModelProperty(value = "版本号")
    @Excel(name = "版本号")
    private String versionNumber;

    /** 登记号 */
    @ApiModelProperty(value = "登记号")
    @Excel(name = "登记号")
    private String registrationMark;

    /** 登记批准日期 */
    @ApiModelProperty(value = "登记批准日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "登记批准日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date regTime;

    /** 首次发表日期 */
    @ApiModelProperty(value = "首次发表日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "首次发表日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date pushTime;

    /** 状态[1:可用,2:不可用] */
    @ApiModelProperty(value = "状态[1:可用,2:不可用]")
    @Excel(name = "状态[1:可用,2:不可用]")
    private String status;

    /** 对应主产品id */
    @ApiModelProperty(value = "对应主产品id")
    @Excel(name = "对应主产品id")
    private Long productId;

}