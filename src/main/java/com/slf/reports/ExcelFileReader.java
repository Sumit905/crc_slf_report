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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.slf.reports.utils.ConstantUtil.CATEGORIZATION;
import static com.slf.reports.utils.ConstantUtil.DATA_CLARIFICATION;
import static com.slf.reports.utils.ConstantUtil.DATA_CORRECTION;
import static com.slf.reports.utils.ConstantUtil.DATA_MISSING;
import static com.slf.reports.utils.ConstantUtil.DATE;

@Component
public class ExcelFileReader {

    private MultipartFile excel;


    public MultipartFile getExcel() {
        return excel;
    }

    public void setExcel(MultipartFile excel) {
        this.excel = excel;
    }

    private static final String  SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    private static final String  PRIORITY = "PRIORITY";
    private static final String  INC_NO = "INC_NO";



    XSSFWorkbook fetchWorkBookReader() throws IOException {
        return new XSSFWorkbook(getExcel().getInputStream());
    }

    public List<String> fetchSheetNames() throws ParseException, IOException {
        List<String> sheetNames = new ArrayList<>();
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
            Map<Integer, String> headerDetails = new HashMap<>();
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
                            case CATEGORIZATION:
                                if (sheet.getRow(j).getCell(z) != null) {
                                    String value = sheet.getRow(j).getCell(z).getStringCellValue();
                                    if(value.trim().toUpperCase().contains(DATA_CLARIFICATION)) {
                                        value = DATA_CLARIFICATION;
                                    } else if(value.trim().toUpperCase().contains(DATA_MISSING) || value.trim().toUpperCase().contains(DATA_CORRECTION)) {
                                        value= DATA_CORRECTION;
                                    } else {
                                        value = value.toUpperCase();
                                    }
                                    details.setCategorization(value);
                                } else {
                                    details.setCategorization(DATA_CLARIFICATION);
                                }
                                break;
                            case DATE:
                                if (sheet.getRow(j).getCell(z) != null) {
                                    SimpleDateFormat targetDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
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
                            case PRIORITY:
                                if (sheet.getRow(j).getCell(z) != null) {
                                    details.setPriority(sheet.getRow(j).getCell(z).getStringCellValue().trim().toUpperCase());
                                } else {
                                    details.setPriority("low".toUpperCase());
                                }

                                break;
                            case INC_NO:
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
            System.out.println(Arrays.toString(ioException.getStackTrace()));
        }
        return reportDetailsList;
    }

}
