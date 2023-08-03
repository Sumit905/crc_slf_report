package com.slf.reports.controller;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.response.ResponseModel;
import com.slf.reports.response.Result;
import com.slf.reports.response.StackedColumnModel;
import com.slf.reports.service.SlfReportService;
import com.slf.reports.utils.FridayAndThursdayDates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExcelRestController {

	@Autowired
	private SlfReportService slfReportService;

	@PostMapping("excel")
	public ResponseEntity<String> excelReader(@RequestParam("file") MultipartFile excel) throws ParseException, IOException {
		List<ReportDetails> reportDetailsList = slfReportService.saveReportDetails(excel);
		return new ResponseEntity<String>("Excel file imported successfully.", HttpStatus.OK);
	}


	@PostMapping(path="slfReport/stacked-chart",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfStackedChart(@RequestParam int year){
		List<StackedColumnModel> data = slfReportService.getStackedColumnDetails(year);
		return new ResponseEntity<Object>(data, HttpStatus.OK);
	}
	

	@GetMapping(path="slfReport",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReport(@RequestParam int year)
			throws ParseException, IOException {
		Map<String,ResponseModel>  responseModelMap = new LinkedHashMap<>();
		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(weeklyDate -> {

			List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDate(weeklyDate.getFromDate(), weeklyDate.getToDate());
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
			taskIncident.put("PRB",slfReportDetails.stream().filter(rec -> rec.getStream().equals("PRB".toUpperCase())).count());
			responseModel.setTaskIncident(taskIncident);

			List<ReportDetails> slfReportDetailsLanding = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), "LANDING");
			Map<String,Long> landingIncident = new HashMap<>();
			landingIncident.put("Critical",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("CRITICAL".toUpperCase())).count());
			landingIncident.put("Urgent",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("URGENT".toUpperCase())).count());
			landingIncident.put("High",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("HIGH".toUpperCase())).count());
			landingIncident.put("Medium",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("MEDIUM".toUpperCase())).count());
			landingIncident.put("Low",slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals("LOW".toUpperCase())).count());
			responseModel.setLandingIncident(landingIncident);

			List<ReportDetails> slfReportDetailsIdrs = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), "IDRS");
			Map<String,Long> idrsIncident = new HashMap<>();
			slfReportDetailsIdrs.stream().filter(rec -> rec.getPriority().equalsIgnoreCase("HIGH")).forEach(action-> System.out.println(action.toString()));

			idrsIncident.put("Critical",calculateIncidentNo(slfReportDetailsIdrs,"CRITICAL".toUpperCase()));
			idrsIncident.put("Urgent",calculateIncidentNo(slfReportDetailsIdrs,"URGENT".toUpperCase()));
			idrsIncident.put("High",calculateIncidentNo(slfReportDetailsIdrs,"HIGH".toUpperCase()));
			idrsIncident.put("Medium",calculateIncidentNo(slfReportDetailsIdrs,"MEDIUM".toUpperCase()));
			idrsIncident.put("Low",calculateIncidentNo(slfReportDetailsIdrs,"LOW".toUpperCase()));
			responseModel.setIdsIncident(idrsIncident);

			List<ReportDetails> slfReportDetailsBatchs = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), "CRC-BATCHES");
			Map<String,Long> batchesIncident = new HashMap<>();
			batchesIncident.put("Critical",calculateIncidentNo(slfReportDetailsBatchs,"CRITICAL"));
			batchesIncident.put("Urgent",calculateIncidentNo(slfReportDetailsBatchs,"URGENT"));
			batchesIncident.put("High",calculateIncidentNo(slfReportDetailsBatchs,"HIGH"));
			batchesIncident.put("Medium",calculateIncidentNo(slfReportDetailsBatchs,"MEDIUM"));
			batchesIncident.put("Low",calculateIncidentNo(slfReportDetailsBatchs,"LOW"));
			responseModel.setBatchesIncident(batchesIncident);

			List<ReportDetails> slfReportDetailsOpen = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), "IPIX");
			Map<String,Long> openShiftIncident = new HashMap<>();
			openShiftIncident.put("Critical",calculateIncidentNo(slfReportDetailsOpen,"CRITICAL"));
			openShiftIncident.put("Urgent",calculateIncidentNo(slfReportDetailsOpen,"URGENT"));
			openShiftIncident.put("High",calculateIncidentNo(slfReportDetailsOpen,"HIGH"));
			openShiftIncident.put("Medium",calculateIncidentNo(slfReportDetailsOpen,"MEDIUM"));
			openShiftIncident.put("Low",calculateIncidentNo(slfReportDetailsOpen,"LOW"));
			responseModel.setOpenShiftIncident(openShiftIncident);

			Map<String,Long> dataClarificationIncident = new HashMap<>();
			try {
				slfReportService.fetchSheetNames().stream().forEach(sheetName -> {
					dataClarificationIncident.put(sheetName.getStream(),slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.getStream().toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Clarification".toUpperCase())).count());
				});
			} catch (ParseException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			responseModel.setDataClarificationIncident(dataClarificationIncident);

			Map<String,Long> dataCorrectionIncident = new HashMap<>();
			try {
				slfReportService.fetchSheetNames().stream().forEach(sheetName -> {
					dataCorrectionIncident.put(sheetName.getStream(),slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.getStream().toUpperCase())).filter(rec -> rec.getCategorization().equals("Data Correction".toUpperCase())).count());
				});
			} catch (ParseException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			responseModel.setDataCorrectionIncident(dataCorrectionIncident);
			responseModelMap.put(weeklyDate.getFromDate()+" - "+weeklyDate.getToDate(),responseModel);
		});


		return new ResponseEntity<Object>(responseModelMap, HttpStatus.OK);
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
			 listOfConsumer.put(sheetName.getStream(),slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.getStream())).count())
		 );
		 return new ResponseEntity<Map<String,Long>>(listOfConsumer, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/header-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getHeaderDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableHeaderDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/task-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getTaskDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTaskDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/openshift-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getOpenshiftDetails(@PathVariable int year) {
		Result result = slfReportService.fetchOpenshiftTableDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/landing-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getLandingDetails(@PathVariable int year) {
		Result result = slfReportService.fetchLandingTableDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}
	
	@PostMapping(path="slfReport/batch/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getBatchDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableBatchesDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}
	
	@PostMapping(path="slfReport/idrs/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getIdrsDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableIdrsDetails(year);

		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/data/clarification/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDataClarificationDetails(@PathVariable int year) {
		Result result = slfReportService.fetchDateClarificationDetails(year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}
	@PostMapping(path="slfReport/data/correction/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDataCorrectionDetails(@PathVariable int year) {
		Result result = slfReportService.fetchDateCorrectionDetails(year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
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
