package com.slf.reports.controller;

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

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.response.ResponseModel;
import com.slf.reports.service.SlfReportService;;

@RestController
public class ExcelRestController {

	@Autowired
	private SlfReportService slfReportService;

	@PostMapping("excel")
	public String excelReader(@RequestParam("file") MultipartFile excel) throws ParseException {
		List<String> sheetNames = new ArrayList<String>();
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(excel.getInputStream());

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheetNames.add(workbook.getSheetName(i));
			}

//			System.out.println("noOfsheets :: " + workbook.getNumberOfSheets());
//			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//				XSSFSheet sheet = workbook.getSheetAt(i);
//				System.out.println(
//						"Sheet no.  " + workbook.getSheetName(i) + "  noOfRows :: " + sheet.getPhysicalNumberOfRows());
//
//			}

			// find header index
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet sheet = workbook.getSheetAt(i);
//				if (sheet.getRow(0) != null) {
//					int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
//					Map<String, Integer> headerDetails = new HashMap<String, Integer>();
//					for (int j = 0; j < noOfColumns; j++) {
//						sheet.getRow(0).getCell(j).getStringCellValue();
//						headerDetails.put(sheet.getRow(0).getCell(j).getStringCellValue(), j);
//					}
//					System.out.println("Sheet Name.  " + workbook.getSheetName(i));
//					System.out.println("Headers Name.  ");
//					System.out.println(headerDetails.toString());
//				}

				Map<Integer, String> headerDetails = new HashMap<Integer, String>();
				if (sheet.getRow(0) != null) {
					int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
					for (int j = 0; j <= sheet.getPhysicalNumberOfRows(); j++) {
						if (j == 0 && sheet.getRow(j) != null) {
							for (int z = 0; z < noOfColumns; z++) {
								headerDetails.put(z, sheet.getRow(j).getCell(z).getStringCellValue());
							}
						} else if (sheet.getRow(j) != null) {
							ReportDetails details = new ReportDetails();
							details.setStream(workbook.getSheetName(i));
							for (int z = 0; z < noOfColumns; z++) {
								switch (headerDetails.get(z).toUpperCase()) {
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
										if(sheet.getRow(j).getCell(z).getCellType().name().equals("STRING") && sheet.getRow(j).getCell(z).getRawValue() != null) {
											
											System.out.println(sheet.getRow(j).getCell(z).getStringCellValue()+" :: "+workbook.getSheetName(i));
											if(isValidFormat("yyyy/MM/dd",sheet.getRow(j).getCell(z).getStringCellValue()))
											details.setDate(convertToLocalDateViaSqlDate(new SimpleDateFormat("yyyy/MM/dd").parse(sheet.getRow(j).getCell(z).getStringCellValue())));
											if(isValidFormat("dd-MM-yyyy",sheet.getRow(j).getCell(z).getStringCellValue()))
												details.setDate(convertToLocalDateViaSqlDate(new SimpleDateFormat("dd-MM-yyyy").parse(sheet.getRow(j).getCell(z).getStringCellValue())));
										}else if(sheet.getRow(j).getCell(z).getDateCellValue() != null){
											details.setDate(convertToLocalDateViaSqlDate(sheet.getRow(j).getCell(z).getDateCellValue()));
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
										details.setIncidentNo(sheet.getRow(j).getCell(z).getStringCellValue());
									break;

								}
							}
							slfReportService.saveReportDetails(details);

						}

					}

				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return Arrays.toString(sheetNames.toArray());
	}

	@GetMapping(path="slfReport",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReport(@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate) {
		 List<ReportDetails> slfReportDetails = slfReportService.fatchReportDetailsOnBasesOfDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
		 ResponseModel responseModel = new ResponseModel();
		 
		 Map<String,Long> incident = new HashMap<>();
		 
		 incident.put("Critical",slfReportDetails.stream().filter(rec -> rec.getPriority().equals("Critical".toUpperCase())).count());
		 incident.put("Urgent",slfReportDetails.stream().filter(rec -> rec.getPriority().equals("Urgent".toUpperCase())).count());
		 incident.put("High",slfReportDetails.stream().filter(rec -> rec.getPriority().equals("High".toUpperCase())).count());
		 incident.put("Medium",slfReportDetails.stream().filter(rec -> rec.getPriority().equals("Medium".toUpperCase())).count());
		 incident.put("Low",slfReportDetails.stream().filter(rec -> rec.getPriority().equals("Low".toUpperCase())).count());
		 responseModel.setTotalNoOfIncident(incident);
		 
		 
		 Map<String,Long> taskIncident = new HashMap<>();
		 taskIncident.put("CTASK",slfReportDetails.stream().filter(rec -> rec.getStream().equals("CH & CTASK".toUpperCase())).count());
		 taskIncident.put("SCTASK",slfReportDetails.stream().filter(rec -> rec.getStream().equals("SCTASK".toUpperCase())).count());
		 taskIncident.put("PTASK",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PTASK".toUpperCase())).count());
		 taskIncident.put("RITM",slfReportDetails.stream().filter(rec -> rec.getStream().equals("RITM".toUpperCase())).count());
		 responseModel.setTaskIncident(taskIncident);
		 
		 List<ReportDetails> slfReportDetailsLanding = slfReportService.fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "LANDING");
		 Map<String,Long> landingIncident = new HashMap<>();
		 landingIncident.put("Critical",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("CRITICAL".toUpperCase())).count());
		 landingIncident.put("Urgent",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("URGENT".toUpperCase())).count());
		 landingIncident.put("High",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("HIGH".toUpperCase())).count());
		 landingIncident.put("Medium",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("MEDIUM".toUpperCase())).count());
		 landingIncident.put("Low",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("LOW".toUpperCase())).count());
		 responseModel.setLandingIncident(landingIncident);
		 
		 List<ReportDetails> slfReportDetailsIdrs = slfReportService.fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "IDRS");
		 Map<String,Long> idrsIncident = new HashMap<>();
		 slfReportDetailsIdrs.stream().filter(rec -> rec.getPriority().equalsIgnoreCase("HIGH")).forEach(action-> System.out.println(action.toString()));
		
		 idrsIncident.put("Critical",calculateIncidentNo(slfReportDetailsIdrs,"CRITICAL".toUpperCase()));
		 idrsIncident.put("Urgent",calculateIncidentNo(slfReportDetailsIdrs,"URGENT".toUpperCase()));		 
		 idrsIncident.put("High",calculateIncidentNo(slfReportDetailsIdrs,"HIGH".toUpperCase()));
		 idrsIncident.put("Medium",calculateIncidentNo(slfReportDetailsIdrs,"MEDIUM".toUpperCase()));
		 idrsIncident.put("Low",calculateIncidentNo(slfReportDetailsIdrs,"LOW".toUpperCase()));
		 responseModel.setIdsIncident(idrsIncident);
		 
		 List<ReportDetails> slfReportDetailsBatchs = slfReportService.fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "CRC-BATCHES");
		 Map<String,Long> batchesIncident = new HashMap<>();
		 batchesIncident.put("Critical",calculateIncidentNo(slfReportDetailsBatchs,"CRITICAL"));
		 batchesIncident.put("Urgent",calculateIncidentNo(slfReportDetailsBatchs,"URGENT"));
		 batchesIncident.put("High",calculateIncidentNo(slfReportDetailsBatchs,"HIGH"));
		 batchesIncident.put("Medium",calculateIncidentNo(slfReportDetailsBatchs,"MEDIUM"));
		 batchesIncident.put("Low",calculateIncidentNo(slfReportDetailsBatchs,"LOW"));
		 responseModel.setBatchesIncident(batchesIncident);
		 
		 List<ReportDetails> slfReportDetailsOpen = slfReportService.fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "IPIX");
		 Map<String,Long> openShiftIncident = new HashMap<>();
		 openShiftIncident.put("Critical",calculateIncidentNo(slfReportDetailsOpen,"CRITICAL"));
		 openShiftIncident.put("Urgent",calculateIncidentNo(slfReportDetailsOpen,"URGENT"));
		 openShiftIncident.put("High",calculateIncidentNo(slfReportDetailsOpen,"HIGH"));
		 openShiftIncident.put("Medium",calculateIncidentNo(slfReportDetailsOpen,"MEDIUM"));
		 openShiftIncident.put("Low",calculateIncidentNo(slfReportDetailsOpen,"LOW"));
		 responseModel.setOpenShiftIncident(openShiftIncident);
		 
		 Map<String,Long> dataClarificationIncident = new HashMap<>();
		 dataClarificationIncident.put("CAP-RM-MHS-SL1",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("RIX",slfReportDetails.stream().filter(rec -> rec.getStream().equals("RIX".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("CAP-SEI-BIHSG_WBO-SL2",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("PRICERA",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Pricera".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("CRIPF",slfReportDetails.stream().filter(rec -> rec.getStream().equals("CRIPF".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("cripf tools (critical product information flow)",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("CUST - RRM",slfReportDetails.stream().filter(rec -> rec.getStream().equals("RRM".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("IXP/EDDA",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).count());
		 dataClarificationIncident.put("IKEA POINT OF SALE",slfReportDetails.stream().filter(rec -> rec.getStream().equals("ipos - ikea point of sale".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("IFOOD",slfReportDetails.stream().filter(rec -> rec.getStream().equals("IFOOD".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("IIP(DATA POP)/CTASK",slfReportDetails.stream().filter(rec -> rec.getStream().equals("IIP".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("ikea com",slfReportDetails.stream().filter(rec -> rec.getStream().equals("IKEA COM".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("ISELL",slfReportDetails.stream().filter(rec -> rec.getStream().equals("ISELL".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("PIA-FACTS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PIA-FACTS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("pcm (product change management)	",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PCM".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("SUPPORT TEAM",slfReportDetails.stream().filter(rec -> rec.getStream().equals("SUPPORT TEAM".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("PTAG",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PTAG".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("PLUS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Plus".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("ROIG",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("sams (Service Action Management System)",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("SEKUND APPLICATION",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("DCI",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Digital Customer Integration".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("LCI",slfReportDetails.stream().filter(rec -> rec.getStream().equals("LCI".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("PIA",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PIA".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("MIX",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MIX".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("DSP",slfReportDetails.stream().filter(rec -> rec.getStream().equals("DSP".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("ICI",slfReportDetails.stream().filter(rec -> rec.getStream().equals("ICI".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("SCPIX",slfReportDetails.stream().filter(rec -> rec.getStream().equals("SCPIX".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("RIMS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("rims".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("Athena",slfReportDetails.stream().filter(rec -> rec.getStream().equals("CAP_INT(Athena)".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("IDSS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("idss platform".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("Support Planning-DWP",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Support Planning-DWP".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("Country Range",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Country Range".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("malaysia customs operation manae",slfReportDetails.stream().filter(rec -> rec.getStream().equals("malaysia customs operation mana".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("common landing area",slfReportDetails.stream().filter(rec -> rec.getStream().equals("common landing area".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("supply chain visibility",slfReportDetails.stream().filter(rec -> rec.getStream().equals("supply chain visibility".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("SALJA",slfReportDetails.stream().filter(rec -> rec.getStream().equals("salja".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("TCS-PSQD",slfReportDetails.stream().filter(rec -> rec.getStream().equals("TCS-PDSQ".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 dataClarificationIncident.put("IKEA TRANSPORT MANAGEMENT",slfReportDetails.stream().filter(rec -> rec.getStream().equals("ikea transport management (itm)".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 responseModel.setDataClarificationIncident(dataClarificationIncident);
		 
		 Map<String,Long> dataCorrectionIncident = new HashMap<>();
		 dataCorrectionIncident.put("CAP-RM-MHS-SL1",slfReportDetails.stream().filter(rec -> rec.getStream().equals("MHS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("RIX",slfReportDetails.stream().filter(rec -> rec.getStream().equals("RIX".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("CUST - RRM",slfReportDetails.stream().filter(rec -> rec.getStream().equals("RRM".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("PTAG",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PTAG".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("SCPIX",slfReportDetails.stream().filter(rec -> rec.getStream().equals("SCPIX".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("ISELL",slfReportDetails.stream().filter(rec -> rec.getStream().equals("ISELL".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("Athena",slfReportDetails.stream().filter(rec -> rec.getStream().equals("CAP_INT(Athena)".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("IIP(DATA POP)/CTASK",slfReportDetails.stream().filter(rec -> rec.getStream().equals("IIP".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataClarificationIncident.put("PLUS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Plus".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataClarificationIncident.put("Country Range",slfReportDetails.stream().filter(rec -> rec.getStream().equals("Country Range".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 dataCorrectionIncident.put("PIA-FACTS",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PIA-FACTS".toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 responseModel.setDataCorrectionIncident(dataCorrectionIncident);
		return new ResponseEntity<Object>(responseModel, HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReportConsumer",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer) {
		 List<ReportDetails> slfReportDetails = slfReportService.fatchReportDetails();
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	@GetMapping(path="slfReportConsumerByDate",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer,@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate) {
		
		List<ReportDetails> slfReportDetails = slfReportService.fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), consumer);
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
	    return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
	}
	
	public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }
  
	
	public Long calculateIncidentNo( List<ReportDetails> sqlResult , String priority) {
		
		Long count = 0L;		
		
		for(int i=0;i<sqlResult.size();i++) {
			if(priority.compareTo(sqlResult.get(i).getPriority().trim().toString())==0) {
				count++;
			}
		}
		return count;
	}
	
}
