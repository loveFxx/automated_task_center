package com.sailvan.dispatchcenter.db.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  导入
 * @author mh
 * @date 2021
 */
@Service
public class ImportService implements com.sailvan.dispatchcenter.common.pipe.ImportService {


    /**
     * 处理上传的文件
     *
     * @param in
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public List getBankListByExcel(InputStream in, String fileName) throws Exception {
        List list = new ArrayList<>();
        //创建Excel工作薄
        Workbook work = this.getWorkbook(in, fileName);
        if (null == work) {
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;

        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }

            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                row = sheet.getRow(j);
                if (row == null || row.getFirstCellNum() == j) {
                    continue;
                }

                List<Object> li = new ArrayList<>();
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    // Java读取Excel数值内容带.0或变科学计数法
                    if(cell != null){
                        cell.setCellType(CellType.STRING);
                    }
                    li.add(cell);
                }
                list.add(li);
            }
        }
        work.close();
        return list;
    }

    /**
     * 新的excel处理格式，以头部列大小为准，每行单元格为空的直接判定为null，第一行不能为空
     *
     * @param in
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public List parseExcel(InputStream in, String fileName) throws Exception {
        List list = new ArrayList<>();
        //创建Excel工作薄
        Workbook work = this.getWorkbook(in, fileName);
        if (null == work) {
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;

        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }

            row = sheet.getRow(sheet.getFirstRowNum());
            if (row == null){
                throw new Exception("创建Excel工作薄第一行为空，请设置表头！");
            }else {
                int size = row.getLastCellNum();
                for (int j = sheet.getFirstRowNum() + 1; j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null || row.getFirstCellNum() == j) {
                        continue;
                    }

                    List<Object> li = new ArrayList<>();
                    for (int y = row.getFirstCellNum(); y < size; y++) {
                        cell = row.getCell(y);
                        // Java读取Excel数值内容带.0或变科学计数法
                        if(cell != null){
                            cell.setCellType(CellType.STRING);
                            li.add(cell);
                        }else {
                            li.add("");
                        }
                    }
                    list.add(li);
                }
            }
        }
        work.close();
        return list;
    }

    /**
     * 判断文件格式
     *
     * @param inStr
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook workbook = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        String xls = ".xls";
        String xlsx = ".xlsx";
        if (xls.equals(fileType)) {
            workbook = new HSSFWorkbook(inStr);
        } else if (xlsx.equals(fileType)) {
            workbook = new XSSFWorkbook(inStr);
        } else {
            throw new Exception("请上传excel文件！");
        }
        return workbook;
    }

}
