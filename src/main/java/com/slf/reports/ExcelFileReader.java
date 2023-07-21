package com.slf.reports;

import com.slf.reports.entity.ReportDetails;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class ExcelFileReader {

    private MultipartFile excel;

    private final static List<String> sheetNames = new ArrayList<>();

    public MultipartFile getExcel() {
        return excel;
    }

    public void setExcel(MultipartFile excel) {
        this.excel = excel;
    }



    XSSFWorkbook fetchWorkBookReader() throws ParseException, IOException {
        return new XSSFWorkbook(getExcel().getInputStream());
    }

    public List<String> fetchSheetNames() throws ParseException, IOException {
        XSSFWorkbook workbook = fetchWorkBookReader();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        return sheetNames;
    }


    public List<ReportDetails> fetchExcelData() {
        List<ReportDetails> reportDetailsList = new ArrayList<>();
        try{
        XSSFWorkbook workbook = fetchWorkBookReader();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            Map<Integer, String> headerDetails = new HashMap<Integer, String>();
            int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int j = 0; j <= sheet.getPhysicalNumberOfRows(); j++) {
                if (j == 0 && sheet.getRow(j) != null) {
                    for (int z = 0; z < noOfColumns; z++) {
                        headerDetails.put(z, sheet.getRow(j).getCell(z).getStringCellValue());
                    }
                } else {
                    if(sheet.getRow(j) == null) {
                        sheet.getRow(j);
                        continue;
                    }

                    ReportDetails details = new ReportDetails();
                    details.setStream(workbook.getSheetName(i));

                    for (int z = 0; z < headerDetails.size(); z++) {
                        switch (headerDetails.get(z).trim().toUpperCase()) {
                            case "CATEGORIZATION":
                                if (sheet.getRow(j).getCell(z) != null) {
                                    String value = sheet.getRow(j).getCell(z).getStringCellValue();
                                    if(value.trim().toLowerCase().contains("data clarification")) {
                                        value="Data Clarification".toUpperCase();
                                    } else if(value.trim().toLowerCase().contains("data missing") || value.trim().toLowerCase().contains("data correction")) {
                                        value="Data Correction".toUpperCase();
                                    } else {
                                        value = value.toUpperCase();
                                    }
                                    details.setCategorization(value);
                                } else {
                                    details.setCategorization("Data Clarification".toUpperCase());
                                }
                                break;
                            case "DATE":
                                if (sheet.getRow(j).getCell(z) != null) {
                                    SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    if (sheet.getRow(j).getCell(z).getCellType() == CellType.NUMERIC
                                        && DateUtil.isCellDateFormatted(sheet.getRow(j).getCell(z))) {
                                        Date date = sheet.getRow(j).getCell(z).getDateCellValue();
                                        String formattedDate = targetDateFormat.format(date);
                                        LocalDate localDate = LocalDate.parse(formattedDate, formatter);
                                        details.setDate(localDate);
                                    } else if (sheet.getRow(j).getCell(z).getCellType() == CellType.STRING) {
                                        String cellValue = sheet.getRow(j).getCell(z).getStringCellValue();
                                        try {
                                            Date date = new SimpleDateFormat().parse(cellValue);
                                            String formattedDate = targetDateFormat.format(date);
                                            LocalDate localDate = LocalDate.parse(formattedDate, formatter);
                                            details.setDate(localDate);
                                        } catch (Exception e) {
                                            // Not a valid date, skip
                                        }
                                    }
                                }
                                break;
                            case "PRIORITY":
                                if (sheet.getRow(j).getCell(z) != null) {
                                    details.setPriority(sheet.getRow(j).getCell(z).getStringCellValue().trim().toUpperCase());
                                } else {
                                    details.setPriority("low".toUpperCase());
                                }

                                break;
                            case "INC_NO":
                                if (sheet.getRow(j).getCell(z) != null)
                                    details.setIncidentNo(sheet.getRow(j).getCell(z).getStringCellValue().trim());
                                break;

                        }
                    }
                    reportDetailsList.add(details);
                }
            }
        }

        }catch(IOException ioException){
            System.out.println(ioException.getStackTrace());
        }
        catch (ParseException parseException){
            System.out.println(parseException.getStackTrace());
        }
        return reportDetailsList;
    }

}
