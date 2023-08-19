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
import org.springframework.web.bind.annotation.*;
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


	@PutMapping(path="slfReport", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByDate(@RequestParam String fromDate, @RequestParam String toDate){
		List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
		return new ResponseEntity<Object>(slfReportDetails, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/stacked-chart",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfStackedChart(@RequestParam int year){
		List<StackedColumnModel> data = slfReportService.getTotalNoOfIncChartDetails(year);
		return new ResponseEntity<Object>(data, HttpStatus.OK);
	}
	@PostMapping(path="slfReport/task-chart",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayTaskChart(@RequestParam int year){
		List<StackedColumnModel> data = slfReportService.getTaskIncChartDetails(year);
		return new ResponseEntity<Object>(data, HttpStatus.OK);
	}

	@GetMapping(path="slfReport",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReport(@RequestParam int year){
		return new ResponseEntity<Object>(slfReportService.fetchResponseModels(year), HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumer/{consumer}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@PathVariable("consumer")String consumer) {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetails();
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	@GetMapping(path="slfReport/consumer/date",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer,@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate) {
		
		List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), consumer);
		return new ResponseEntity<Object>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumers",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Long>> displaySlfReportByListOfConsumer() {
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


	
}
