package com.sailvan.dispatchcenter.common.util;

import lombok.SneakyThrows;

import java.io.*;

public class WriteToFileUtil {
    @SneakyThrows
    public static void main(String[] args) {
        writeId("bb", 4111114 + "");
        System.out.println(readId("bb"));
    }

    public static void writeId(String cache, String id) {
        try {
            String content = id + "";
            File file = new File("./cache/" + cache);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdir();
                }
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeIdPath(String cache, String id, String path) {
        try {
            String content = id + "";
            File file = new File(path + cache);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdir();
                }
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String readId(String cache) {
        String fileName = "./cache/" + cache;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readIdByPrefix(String cache,String path) {
        String fileName = path + cache;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
