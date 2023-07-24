package com.slf.reports.controller;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.response.ResponseModel;
import com.slf.reports.response.StackedColumnModel;
import com.slf.reports.service.SlfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExcelRestController {

	@Autowired
	private SlfReportService slfReportService;

	@PostMapping("excel")
	public String excelReader(@RequestParam("file") MultipartFile excel) throws ParseException, IOException {
		List<ReportDetails> reportDetailsList = slfReportService.saveReportDetails(excel);
		return Arrays.toString(reportDetailsList.toArray());
	}


	@PostMapping(path="slfReport/stacked-chart",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfStackedChart(@RequestParam("dateList")Map<String,String> dateListMap){
		List<StackedColumnModel> data = new ArrayList<>();
		return new ResponseEntity<Object>(data, HttpStatus.OK);
	}
	

	@GetMapping(path="slfReport",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReport(@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate)
			throws ParseException, IOException {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
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
		 
		 List<ReportDetails> slfReportDetailsLanding = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "LANDING");
		 Map<String,Long> landingIncident = new HashMap<>();
		 landingIncident.put("Critical",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("CRITICAL".toUpperCase())).count());
		 landingIncident.put("Urgent",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("URGENT".toUpperCase())).count());
		 landingIncident.put("High",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("HIGH".toUpperCase())).count());
		 landingIncident.put("Medium",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("MEDIUM".toUpperCase())).count());
		 landingIncident.put("Low",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("LOW".toUpperCase())).count());
		 responseModel.setLandingIncident(landingIncident);
		 
		 List<ReportDetails> slfReportDetailsIdrs = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "IDRS");
		 Map<String,Long> idrsIncident = new HashMap<>();
		 slfReportDetailsIdrs.stream().filter(rec -> rec.getPriority().equalsIgnoreCase("HIGH")).forEach(action-> System.out.println(action.toString()));
		
		 idrsIncident.put("Critical",calculateIncidentNo(slfReportDetailsIdrs,"CRITICAL".toUpperCase()));
		 idrsIncident.put("Urgent",calculateIncidentNo(slfReportDetailsIdrs,"URGENT".toUpperCase()));		 
		 idrsIncident.put("High",calculateIncidentNo(slfReportDetailsIdrs,"HIGH".toUpperCase()));
		 idrsIncident.put("Medium",calculateIncidentNo(slfReportDetailsIdrs,"MEDIUM".toUpperCase()));
		 idrsIncident.put("Low",calculateIncidentNo(slfReportDetailsIdrs,"LOW".toUpperCase()));
		 responseModel.setIdsIncident(idrsIncident);
		 
		 List<ReportDetails> slfReportDetailsBatchs = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "CRC-BATCHES");
		 Map<String,Long> batchesIncident = new HashMap<>();
		 batchesIncident.put("Critical",calculateIncidentNo(slfReportDetailsBatchs,"CRITICAL"));
		 batchesIncident.put("Urgent",calculateIncidentNo(slfReportDetailsBatchs,"URGENT"));
		 batchesIncident.put("High",calculateIncidentNo(slfReportDetailsBatchs,"HIGH"));
		 batchesIncident.put("Medium",calculateIncidentNo(slfReportDetailsBatchs,"MEDIUM"));
		 batchesIncident.put("Low",calculateIncidentNo(slfReportDetailsBatchs,"LOW"));
		 responseModel.setBatchesIncident(batchesIncident);
		 
		 List<ReportDetails> slfReportDetailsOpen = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), "IPIX");
		 Map<String,Long> openShiftIncident = new HashMap<>();
		 openShiftIncident.put("Critical",calculateIncidentNo(slfReportDetailsOpen,"CRITICAL"));
		 openShiftIncident.put("Urgent",calculateIncidentNo(slfReportDetailsOpen,"URGENT"));
		 openShiftIncident.put("High",calculateIncidentNo(slfReportDetailsOpen,"HIGH"));
		 openShiftIncident.put("Medium",calculateIncidentNo(slfReportDetailsOpen,"MEDIUM"));
		 openShiftIncident.put("Low",calculateIncidentNo(slfReportDetailsOpen,"LOW"));
		 responseModel.setOpenShiftIncident(openShiftIncident);
		 
		 Map<String,Long> dataClarificationIncident = new HashMap<>();
		 slfReportService.fetchSheetNames().stream().forEach(sheetName -> {
			 dataClarificationIncident.put(sheetName,slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
		 });
		 responseModel.setDataClarificationIncident(dataClarificationIncident);
		 
		 Map<String,Long> dataCorrectionIncident = new HashMap<>();
		slfReportService.fetchSheetNames().stream().forEach(sheetName -> {
			 dataCorrectionIncident.put(sheetName,slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
		 });
		 responseModel.setDataCorrectionIncident(dataCorrectionIncident);
		return new ResponseEntity<Object>(responseModel, HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumer",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer) {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetails();
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	@GetMapping(path="slfReport/consumer/date",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer,@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate) {
		
		List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), consumer);
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumers",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Long>> displaySlfReportByListOfConsumer() throws ParseException, IOException {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetails();
		 Map<String,Long> listOfConsumer = new HashMap<String, Long>();
		 slfReportService.fetchSheetNames().stream().forEach(sheetName ->
			 listOfConsumer.put(sheetName,slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName)).count())
		 );
		 return new ResponseEntity<Map<String,Long>>(listOfConsumer, HttpStatus.OK);
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
