package com.sailvan.dispatchcenter.common.pipe;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;

/**
 *  导入
 * @author mh
 * @date 2021
 */
public interface ImportService {


    /**
     * 处理上传的文件
     *
     * @param in
     * @param fileName
     * @return
     * @throws Exception
     */
    public List getBankListByExcel(InputStream in, String fileName) throws Exception;

    public List parseExcel(InputStream in, String fileName) throws Exception;

    /**
     * 判断文件格式
     *
     * @param inStr
     * @param fileName
     * @return
     * @throws Exception
     */
    public Workbook getWorkbook(InputStream inStr, String fileName) throws Exception ;

}
