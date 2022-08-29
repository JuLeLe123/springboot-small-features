package com.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author myf
 * @Date 2022/8/26 14:37
 * @description 定义建表语句需要的信息
 */

@Data
public class TableEntity {
    /*列信息*/
    private List<ColumnEntity> entities;
    /*表名*/
    private String tableName;
}

