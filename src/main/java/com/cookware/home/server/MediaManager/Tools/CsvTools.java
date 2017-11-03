package com.cookware.home.server.MediaManager.Tools;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by Kody on 16/10/2017.
 */
public class CsvTools {
    private static final Logger log = Logger.getLogger(CsvTools.class);
    private final DirectoryTools directoryTools = new DirectoryTools();

    public boolean createFile(String fileName){
        try {
            File file = new File(fileName);

            if (file.createNewFile()){
                log.info("Created new ScheduleManager config");
                return true;
            }
            else {
                return false;
            }
        }
        catch (IOException e) {
            log.error(e);
            return false;
        }
    }

    public void writeStringArrayToCsv(String fileName, String[][] data){
        File file = new File(fileName);
        boolean firstValue;

        if (!file.exists()){
            log.error("ScheduleManager config table does not exist");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String[] rowInTable:data) {
            firstValue = true;
            for (String valueInRow:rowInTable){
                if(firstValue){
                    firstValue = false;
                }
                else {
                    sb.append(",");
                }
                sb.append(valueInRow);
            }
            sb.append("\n");
        }

        try {
            FileWriter writer = new FileWriter(fileName);
            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String[][] getStringArrayFromCsv(String fileName){
        String[][] table;
        File file = new File(fileName);
        String content;

        if (!file.exists()){
            log.error("ScheduleManager config table does not exist");
            return null;
        }

        content = getTextFromFile(file);
        String[] rows = content.split("\n");
        table = new String[rows.length][rows[0].split(",").length];

        int count = 0;
        for (String row:rows){
            table[count] = row.split(",");
            count ++;
        }

        return table;
    }

    private String getTextFromFile(File file){
        FileInputStream inputStream = null;
        try {
            String result;
            inputStream = new FileInputStream(file);
            result = readStream(inputStream);
            inputStream.close();
            return result;
        } catch (FileNotFoundException e) {
            log.error("Config file not found", e);
            return null;
        } catch (IOException e) {
            log.error("Cannot close file input stream",e);
            return null;
        }
    }

    private String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            int character = 0;
            while ((character = reader.read()) != -1) {
                sb.append((char) character);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private String printStringArray(String[][] stringArray){
        StringBuilder stringBuilder = new StringBuilder();
        for(String[] row: stringArray){
            for (String value: row){
                stringBuilder.append(value);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}
