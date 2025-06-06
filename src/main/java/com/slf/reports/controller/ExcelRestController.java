package com.slf.reports.controller;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.response.Result;
import com.slf.reports.response.StackedColumnModel;
import com.slf.reports.service.SlfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExcelRestController {

	@Autowired
	private SlfReportService slfReportService;

	@PostMapping(value = "excel",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> excelReader(@RequestParam("file") MultipartFile excel) throws ParseException, IOException {
		List<ReportDetails> reportDetailsList = slfReportService.saveReportDetails(excel);
		return new ResponseEntity<>("Excel file imported successfully.", HttpStatus.OK);
	}


	@PutMapping(path="slfReport", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByDate(@RequestParam String fromDate, @RequestParam String toDate){
		List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
		return new ResponseEntity<>(slfReportDetails, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/total-incident/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfStackedChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTotalNoOfIncChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	@PostMapping(path="slfReport/task-details/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayTaskChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTaskIncChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}
	@PostMapping(path="slfReport/openshift-details/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayOpenShiftChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTotalOpenShiftChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/landing-details/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayLandingChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTotalLandingChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/batch-details/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayBatchesChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTotalBatchesChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/idrs-details/chart/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displayIdrsChart(@PathVariable int year){
		List<StackedColumnModel> data = slfReportService.getTotalIdresChartDetails(year);
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	@GetMapping(path="slfReport",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReport(@RequestParam int year){
		return new ResponseEntity<>(slfReportService.fetchResponseModels(year), HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumer/{consumer}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@PathVariable("consumer")String consumer) {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetails();
		return new ResponseEntity<>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	@GetMapping(path="slfReport/consumer/date",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> displaySlfReportByConsumer(@RequestParam("consumer")String consumer,@RequestParam("fromDate")String fromDate, @RequestParam("toDate")String toDate) {
		
		List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate.parse(fromDate), LocalDate.parse(toDate), consumer);
		return new ResponseEntity<>(slfReportDetails.stream().filter(rec -> rec.getStream().equals(consumer.toUpperCase())), HttpStatus.OK);
	}
	
	
	@GetMapping(path="slfReport/consumers",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String,Long>> displaySlfReportByListOfConsumer() {
		 List<ReportDetails> slfReportDetails = slfReportService.fetchReportDetails();
		 Map<String,Long> listOfConsumer = new HashMap<String, Long>();
		 slfReportService.fetchSheetNames().forEach(sheetName ->
			 listOfConsumer.put(sheetName.getStream(),slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.getStream())).count())
		 );
		 return new ResponseEntity<>(listOfConsumer, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/total-incident/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getHeaderDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableHeaderDetails(year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/task-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getTaskDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTaskDetails(year);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/openshift-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getOpenshiftDetails(@PathVariable int year) {
		Result result = slfReportService.fetchOpenshiftTableDetails(year);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/landing-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getLandingDetails(@PathVariable int year) {
		Result result = slfReportService.fetchLandingTableDetails(year);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PostMapping(path="slfReport/batch-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getBatchDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableBatchesDetails(year);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PostMapping(path="slfReport/idrs-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getIdrsDetails(@PathVariable int year) {
		Result result = slfReportService.fetchTableIdrsDetails(year);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/data/clarification-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDataClarificationDetails(@PathVariable int year) {
		Result result = slfReportService.fetchDateClarificationDetails(year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}
	@PostMapping(path="slfReport/data/correction-details/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDataCorrectionDetails(@PathVariable int year) {
		Result result = slfReportService.fetchDateCorrectionDetails(year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}



	@PostMapping(path="slfReport/{tab}/{priority}/{columnId}/{year}",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDetailsOfInc(@PathVariable String tab,@PathVariable String priority,@PathVariable String columnId,@PathVariable int year) {
		Result result = slfReportService.fetchDetailsOfInc(tab,priority,columnId,year);
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}


	@PostMapping(path="slfReport/monthly",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<StackedColumnModel>> getMontlyDetails(@RequestParam String fromDate, @RequestParam String toDate) {
		List<StackedColumnModel> result = slfReportService.fetchDetailsOfIncident(LocalDate.parse(fromDate), LocalDate.parse(toDate));
		return new ResponseEntity<List<StackedColumnModel>>(result, HttpStatus.OK);
	}

	@PostMapping(path="slfReport/data/monthly-details",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Result> getDataMonthlyDetails(@RequestParam String fromDate, @RequestParam String toDate) {
		Result result = slfReportService.fetchMonthlyDetails(LocalDate.parse(fromDate), LocalDate.parse(toDate));
		return new ResponseEntity<Result>(result, HttpStatus.OK);
	}


	
}
