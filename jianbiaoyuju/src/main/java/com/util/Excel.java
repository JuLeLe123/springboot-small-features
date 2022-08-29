package com.util;

import com.entity.ColumnEntity;
import com.entity.TableEntity;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author myf
 * @Date 2022/8/26 14:39
 * @description 生成SQL语句Excel
 */
public class Excel {
    /**
     * 读取excel并转换为表实体
     *
     * @param excelPath
     * @return
     */
    public TableEntity readExcel(String excelPath) {
        // 解析模板对象List
        List<ColumnEntity> entities = new ArrayList<ColumnEntity>();
        //表名
        String tableName = null;
        try {
            File excel = new File(excelPath);
            if (excel.isFile() && excel.exists()) {//判断文件是否存在
                String[] split = excel.getName().split("\\."); // .是特殊字符，需要转义
                Workbook wb;
                // 根据文件后缀（xls/xlsx）进行判断
                if ("xls".equals(split[1])) {
                    FileInputStream fis = new FileInputStream(excel); // 文件流对象
                    wb = new HSSFWorkbook(fis);
                } else if ("xlsx".equals(split[1])) {
                    wb = new XSSFWorkbook(excel);
                } else {
                    System.out.println("文件类型错误!");
                    return null;
                }
                //开始解析
                Sheet sheet = wb.getSheetAt(0); //获取第一个工作薄
                int firstRowIndex = sheet.getFirstRowNum() + 3; // 第一、二、三行是列名，所以不读，从第四行开始读
                int lastRowIndex = sheet.getLastRowNum();
//                System.out.println("firstRowIndex: " + firstRowIndex);
//                System.out.println("lastRowIndex: " + lastRowIndex);

                //解析模板对象list
                entities = new ArrayList<ColumnEntity>();
                //获取表名
                tableName = sheet.getRow(0).getCell(1).getStringCellValue();

                for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {//遍历行
                    // System.out.println("rIndex: " + rIndex);
                    ColumnEntity entity = new ColumnEntity();
                    Row row = sheet.getRow(rIndex);
                    if (row != null) {
                        //当单元格不为空时的处理逻辑
                        if (row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue() == null || row
                                .getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                            continue;
                        }
                        //解析对象
                        entity.setPrimaryKey("PK".equalsIgnoreCase(row.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));//是否为主键
                        entity.setPhysicalColumnName(row.getCell(3, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());//英文列名
                        entity.setLogicalColumnName(row.getCell(4, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());//中文列名
                        entity.setType(row.getCell(5, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());//字段属性
                        entity.setDisCrib(row.getCell(6, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());//备注
                        entity.setNotNull("是".equals(row.getCell(7, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()) ||
                                "成人必填".equals(row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()) ||
                                "儿童必填".equals(row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));//是否必填
                        //存入list
                        entities.add(entity);
                    }
                }
            } else {
                System.out.println("找不到指定的文件");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TableEntity tableEntity = new TableEntity();
        tableEntity.setEntities(entities);
        tableEntity.setTableName(tableName);
        return tableEntity;
    }

    /**
     * 将表实体转换为sql并输出为txt
     *
     * @param tableEntity
     */
    public void convertSQL(TableEntity tableEntity) {
        StringBuffer sql = new StringBuffer();
        //拼接sql语句
        sql.append("CREATE TABLE ");
        //拼接表名
        sql.append(tableEntity.getTableName());
        sql.append(" (");
        sql.append("\r\n");
        List<ColumnEntity> cellEnties = tableEntity.getEntities();
        //主键
        String primaryKey = null;
        //获取主键
        for (ColumnEntity item : cellEnties) {
            //将primaryKey为true的设为主键
            if (item.isPrimaryKey()) {
                primaryKey = item.getPhysicalColumnName();
                break;
            }
        }
        //循环列
        for (ColumnEntity item : cellEnties) {
            //拼接英文列名字段部分
            sql.append("   " + item.getPhysicalColumnName().trim() + "  ");
            //拼接字段属性
            sql.append(item.getType().trim());
            //分两种情况：1.必填not null 2.不是必填就不加
            if (item.isNotNull()){//如果不允许为空，则拼接NOT NULL
                sql.append(" " + "NOT NULL");
            }
            sql.append("," + "\r\n");
        }
        //拼接定义主键
        sql.append("    constraint "+tableEntity.getTableName()+" primary key(" + primaryKey + ") ); ");
        sql.append("\r\n");

        //拼接中文列名以及备注
        for (ColumnEntity item : cellEnties) {
            sql.append("      comment on column " + tableEntity.getTableName() + "." + item.getPhysicalColumnName() + " is ' " + item.getLogicalColumnName() + " " + item.getDisCrib() + "';");
            sql.append("\r\n");
        }
        System.err.println(sql);

        //导出.txt文件
        try {
            writeTXT("", sql.toString(), tableEntity.getTableName());
            System.out.println("已导出:" + tableEntity.getTableName() + ".txt!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("导出文件失败");
        }
    }
    /**
     * 将字符串写入txt并导出
     *
     * @throws Exception
     */
    public void writeTXT(String path, String value, String fileName) throws Exception {
        File f = new File(path + fileName + ".txt");
        FileOutputStream fos1 = new FileOutputStream(f);
        OutputStreamWriter dos1 = new OutputStreamWriter(fos1);
        dos1.write(value);
        dos1.close();
    }
}
