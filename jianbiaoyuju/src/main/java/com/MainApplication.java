package com;

import java.util.Scanner;

import com.entity.TableEntity;
import com.util.Excel;

/**
 * @Author myf
 * @Date 2022/8/26 14:43
 * @description 通过控制台输入Excel路径，会在jar包同级目录下生成包含建表SQL的TXT文件。
 */
public class MainApplication {
        public static void main(String[] args) {
            while(true) {
                Excel excel = new Excel();
                Scanner scanner = new Scanner(System.in);
                System.err.println("请输入Excel的路径：");
                String excelPath = scanner.nextLine();
                TableEntity tEntity = excel.readExcel(excelPath);
                if(tEntity==null) {
                    continue;
                }
                excel.convertSQL(tEntity);
            }
        }
    }

