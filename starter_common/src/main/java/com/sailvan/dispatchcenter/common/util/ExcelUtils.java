package com.sailvan.dispatchcenter.common.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * @program: automated_task_center
 * @description: 写入机器等统计信息的工具类
 * @author: Wu Xingjian
 * @create: 2021-07-06 10:26
 **/


@Component
public class ExcelUtils {

    /**
     * 单元格里字符最大长度
     * excel2003单元格字符长度上限为32767
     */
    static final int CELL_MAX_LENGTH=10000;
    /**
     * 单元格最大宽度
     */
    static final int CELL_MAX_WIDTH=65280;

    /**
     * https://blog.csdn.net/XlxfyzsFdblj/article/details/104159203
     * XSSFWorkbook xlsx 格式 慢 支持65536行以上
     * @param excelPath
     * @param titles
     * @param infoList
     */
    public static void createExcelFile(String excelPath, String[] titles, ArrayList<String[]> infoList) {
        try {
            File file = new File(excelPath);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(excelPath);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet xssfSheet = workbook.createSheet();

            //TODO 添加表头
            XSSFRow titleRow = xssfSheet.createRow(0);
            for (int j = 0; j < titles.length; j++) {
                XSSFCell cell = titleRow.createCell(j);
                cell.setCellValue(titles[j]);
            }

            //已经写入的条目数 不等于实际行数(有些条目可能占据多行)
            int infoCount=0;
            //实际行数
            int lineCount=0;
            while(infoCount<infoList.size())   {

                //从第二行开始写入sheet
                XSSFRow row = xssfSheet.createRow(lineCount+1);
                String[] curStr = infoList.get(infoCount);
                lineCount++;
                infoCount++;
                for (int j = 0; j < curStr.length; j++) {
                    XSSFCell cell = row.createCell(j);



                    //TODO 一个单元格过长
                    if (curStr[j].length()>CELL_MAX_LENGTH) {

                        ArrayList<String> stringArr = splitLongString(curStr[j],',',CELL_MAX_LENGTH);
                        //第一次 还是本行
                        cell.setCellValue( stringArr.get(0));
                        setCellWidth(xssfSheet,curStr[j],j);
                        stringArr = new ArrayList<String>(stringArr.subList(1,stringArr.size()));

                        //第二次以后 新的行
                        for ( String string : stringArr) {
                            XSSFRow newRow = xssfSheet.createRow(lineCount+1);
                            lineCount++;
                            XSSFCell newCell = newRow.createCell(j);
                            newCell.setCellValue(string);
                            setCellWidth(xssfSheet,curStr[j],j);
                        }
                    }
                    else{
                        cell.setCellValue(curStr[j]);
                        setCellWidth(xssfSheet,curStr[j],j);
                    }

                }
            }

            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void createExcelFile2(String excelPath, String[] titles, ArrayList<String[]> infoList) {
        try {
            File file = new File(excelPath);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(excelPath);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet xssfSheet = workbook.createSheet();

            //TODO 添加表头
            HSSFRow titleRow = xssfSheet.createRow(0);
            for (int j = 0; j < titles.length; j++) {
                HSSFCell cell = titleRow.createCell(j);
                cell.setCellValue(titles[j]);
            }

            //已经写入的条目数 不等于实际行数(有些条目可能占据多行)
            int infoCount=0;
            //实际行数
            int lineCount=0;
            while(infoCount<infoList.size())   {

                //从第二行开始写入sheet
                HSSFRow row = xssfSheet.createRow(lineCount+1);
                String[] curStr = infoList.get(infoCount);
                lineCount++;
                infoCount++;
                for (int j = 0; j < curStr.length; j++) {
                    HSSFCell cell = row.createCell(j);



                    //TODO 一个单元格过长
                    if (curStr[j].length()>CELL_MAX_LENGTH) {

                        ArrayList<String> stringArr = splitLongString(curStr[j],',',CELL_MAX_LENGTH);
                        //第一次 还是本行
                        cell.setCellValue( stringArr.get(0));
                        setCellWidth2(xssfSheet,curStr[j],j);
                        stringArr = new ArrayList<String>(stringArr.subList(1,stringArr.size()));

                        //第二次以后 新的行
                        for ( String string : stringArr) {
                            HSSFRow newRow = xssfSheet.createRow(lineCount+1);
                            lineCount++;
                            HSSFCell newCell = newRow.createCell(j);
                            newCell.setCellValue(string);
                            setCellWidth2(xssfSheet,curStr[j],j);
                        }
                    }
                    else{
                        cell.setCellValue(curStr[j]);
                        setCellWidth2(xssfSheet,curStr[j],j);
                    }

                }
            }

            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void createExcelFile3(String excelPath, String[] titles, ArrayList<String[]> infoList) {

        SXSSFWorkbook workbook=null;
        try {
            File file = new File(excelPath);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(excelPath);
            workbook = new SXSSFWorkbook();
            SXSSFSheet xssfSheet = workbook.createSheet();

            //TODO 添加表头
            SXSSFRow titleRow = xssfSheet.createRow(0);
            for (int j = 0; j < titles.length; j++) {
                SXSSFCell cell = titleRow.createCell(j);
                cell.setCellValue(titles[j]);
            }

            //已经写入的条目数 不等于实际行数(有些条目可能占据多行)
            int infoCount=0;
            //实际行数
            int lineCount=0;
            while(infoCount<infoList.size())   {

                //从第二行开始写入sheet
                SXSSFRow row = xssfSheet.createRow(lineCount+1);
                String[] curStr = infoList.get(infoCount);
                lineCount++;
                infoCount++;
                for (int j = 0; j < curStr.length; j++) {
                    SXSSFCell cell = row.createCell(j);


                    if (curStr[j] == null) {
                        continue;
                    }
                    //TODO 一个单元格过长
                    if (curStr[j].length()>CELL_MAX_LENGTH) {

                        ArrayList<String> stringArr = splitLongString(curStr[j],',',CELL_MAX_LENGTH);
                        //第一次 还是本行
                        cell.setCellValue( stringArr.get(0));
                        setCellWidth3(xssfSheet,curStr[j],j);
                        stringArr = new ArrayList<String>(stringArr.subList(1,stringArr.size()));

                        //第二次以后 新的行
                        for ( String string : stringArr) {
                            SXSSFRow newRow = xssfSheet.createRow(lineCount+1);
                            lineCount++;
                            SXSSFCell newCell = newRow.createCell(j);
                            newCell.setCellValue(string);
                            setCellWidth3(xssfSheet,curStr[j],j);
                        }
                    }
                    else{
                        cell.setCellValue(curStr[j]);
                        setCellWidth3(xssfSheet,curStr[j],j);
                    }

                }
            }

            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(workbook != null){
                workbook.dispose();
            }
        }
    }


    /**
     * 通过拆分标志拆分长度超过cell限度的字符串
     * 保证在拆分标志位断开 且每个长度不超过splitLimit
     * splitLimit需要大于最长一项
     */
    public  static ArrayList<String> splitLongString(String longStr, char splitSign, int splitLimit){


        ArrayList<String> res=new ArrayList<>();
        int start=0,end=0;
        while(true){
            end=start+splitLimit;
            if (end>=longStr.length()) {
                end=longStr.length();
                res.add(longStr.substring(start,end));
                return res;
            }
            if (longStr.charAt(end) != splitSign) {
                String subStr = longStr.substring(start, end);
                if (subStr.lastIndexOf(splitSign)<0) {
                    ArrayList<String> errorRes=new ArrayList<>();
                    errorRes.add("splitLongString方法的splitLimit太短了 要大于最长项长度+1");
                    return errorRes ;
                }
                end = start+subStr.lastIndexOf(splitSign);
                //end要覆盖逗号
                end++;
            }
            res.add(longStr.substring(start,end));
            start=end;

        }
    }

    /**
     * 根据字符串长度设置单元格宽度
     * 若超过宽度上限则设为上限值
     * @param xssfSheet
     */
    public static void setCellWidth(XSSFSheet xssfSheet,String curStr,int curColumnIndex){

        if (curStr != null) {
            int curLength=curStr.length()*300;
            if (curLength > xssfSheet.getColumnWidth(curColumnIndex)) {

                if (curLength > CELL_MAX_WIDTH) {
                    xssfSheet.setColumnWidth(curColumnIndex, CELL_MAX_WIDTH);
                } else {
                    xssfSheet.setColumnWidth(curColumnIndex, curLength);

                }

            }
        }

    }


    public static void setCellWidth2(HSSFSheet xssfSheet,String curStr,int curColumnIndex){

        if (curStr != null) {
            int curLength=curStr.length()*300;
            if (curLength > xssfSheet.getColumnWidth(curColumnIndex)) {

                if (curLength > CELL_MAX_WIDTH) {
                    xssfSheet.setColumnWidth(curColumnIndex, CELL_MAX_WIDTH);
                } else {
                    xssfSheet.setColumnWidth(curColumnIndex, curLength);

                }

            }
        }

    }


    public static void setCellWidth3(SXSSFSheet xssfSheet,String curStr,int curColumnIndex){

        if (curStr != null) {
            int curLength=curStr.length()*300;
            if (curLength > xssfSheet.getColumnWidth(curColumnIndex)) {

                if (curLength > CELL_MAX_WIDTH) {
                    xssfSheet.setColumnWidth(curColumnIndex, CELL_MAX_WIDTH);
                } else {
                    xssfSheet.setColumnWidth(curColumnIndex, curLength);

                }

            }
        }

    }



}



