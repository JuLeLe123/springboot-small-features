package com.entity;

import lombok.Data;

/**
 * @Author myf
 * @Date 2022/8/26 14:37
 * @description 定义我们关注的字段的关键信息
 */

@Data
public class ColumnEntity {
    /*是否主键*/
    private boolean isPrimaryKey;
    /*英文列名*/
    private String physicalColumnName;
    /*中文列名*/
    private String logicalColumnName;
    /*字段属性*/
    private String type;
    /*备注*/
    private String disCrib;
    /*是否必填*/
    private boolean isNotNull;
}

