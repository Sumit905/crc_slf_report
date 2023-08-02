package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.slf.reports.ExcelFileReader;
import com.slf.reports.response.DataPointsModel;
import com.slf.reports.response.HeaderDetails;
import com.slf.reports.response.Result;
import com.slf.reports.response.StackedColumnModel;
import com.slf.reports.utils.FridayAndThursdayDates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.repository.SlfReportRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
class SlfReportServiceImpl implements SlfReportService {

	@Autowired
	private SlfReportRepository reportRepository;

	@Autowired
	private ExcelFileReader excelFileReader;

	@Transactional
	public List<ReportDetails> saveReportDetails(MultipartFile excel) throws ParseException, IOException {
		excelFileReader.setExcel(excel);
		List<ReportDetails> reportDetailsList = excelFileReader.fetchExcelData();
		reportDetailsList.removeIf(rec-> rec.getIncidentNo()==null);
		reportDetailsList.removeIf(rec-> rec.getDate()==null);
		reportDetailsList.stream().filter(rec -> rec.getCategorization() == null).forEach(rec -> rec.setCategorization("JOB FAILURE"));
		reportDetailsList.stream().filter(rec -> rec.getPriority() == null).forEach(rec -> rec.setPriority("NA"));
		return  convertIteratorToList(reportRepository.saveAll(reportDetailsList).iterator());
	}
	
	public List<ReportDetails> fetchReportDetails(){
		return (List<ReportDetails>) reportRepository.findAll();
	}
	
	
	public List<ReportDetails> fetchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate) {
		
		return reportRepository.findAllByDateBetween(fromDate,toDate);
	}

	@Override
	public List<ReportDetails> fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate, LocalDate toDate,
			String consumer) {
		
		return reportRepository.findConsumerByDateBetween(fromDate,toDate,consumer);
	}

	public static <T> List<T> convertIteratorToList(Iterator<T> iterator) {
		Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false)
							.collect(Collectors.toList());
	}

	public List<String> fetchSheetNames() throws ParseException, IOException {
		return excelFileReader.fetchSheetNames();
	}

	@Override
	public List<StackedColumnModel> getStackedColumnDetails(int year) {

		List<StackedColumnModel> data = new ArrayList<>();
		StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
		criticalStackedColumnModel.setType("stackedColumn");
		criticalStackedColumnModel.setName("Critical");
		criticalStackedColumnModel.setShowInLegend("true");
		criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
		StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
		urgentStackedColumnModel.setType("stackedColumn");
		urgentStackedColumnModel.setName("Urgent");
		urgentStackedColumnModel.setShowInLegend("true");
		urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
		StackedColumnModel highStackedColumnModel = new StackedColumnModel();
		highStackedColumnModel.setType("stackedColumn");
		highStackedColumnModel.setName("High");
		highStackedColumnModel.setShowInLegend("true");
		highStackedColumnModel.setyValueFormatString("## High Incidents");

		StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
		mediumStackedColumnModel.setType("stackedColumn");
		mediumStackedColumnModel.setName("Medium");
		mediumStackedColumnModel.setShowInLegend("true");
		mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

		StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
		lowStackedColumnModel.setType("stackedColumn");
		lowStackedColumnModel.setName("Low");
		lowStackedColumnModel.setShowInLegend("true");
		lowStackedColumnModel.setyValueFormatString("## Low Incidents");


		List<DataPointsModel> criticalDataPoint = new ArrayList<>();
		List<DataPointsModel> urgentDataPoint = new ArrayList<>();
		List<DataPointsModel> highDataPoint = new ArrayList<>();
		List<DataPointsModel> mediumDataPoint = new ArrayList<>();
		List<DataPointsModel> lowDataPoint = new ArrayList<>();

		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			DataPointsModel dataPointsModel = new DataPointsModel();
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
			dataPointsModel.setY(slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase())).count());
			dataPointsModel.setLabel(rec.getFromDate()+" - "+rec.getToDate());
			criticalDataPoint.add(dataPointsModel);
			dataPointsModel = new DataPointsModel();
			dataPointsModel.setY(slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase())).count());
			dataPointsModel.setLabel(rec.getFromDate()+" - "+rec.getToDate());
			urgentDataPoint.add(dataPointsModel);
			dataPointsModel = new DataPointsModel();
			dataPointsModel.setY(slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase())).count());
			dataPointsModel.setLabel(rec.getFromDate()+" - "+rec.getToDate());
			highDataPoint.add(dataPointsModel);
			dataPointsModel = new DataPointsModel();
			dataPointsModel.setY(slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase())).count());
			dataPointsModel.setLabel(rec.getFromDate()+" - "+rec.getToDate());
			mediumDataPoint.add(dataPointsModel);
			dataPointsModel = new DataPointsModel();
			dataPointsModel.setY(slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase())).count());
			dataPointsModel.setLabel(rec.getFromDate()+" - "+rec.getToDate());
			lowDataPoint.add(dataPointsModel);

		});
		criticalStackedColumnModel.setDataPoints(criticalDataPoint);
		urgentStackedColumnModel.setDataPoints(urgentDataPoint);
		highStackedColumnModel.setDataPoints(highDataPoint);
		mediumStackedColumnModel.setDataPoints(mediumDataPoint);
		lowStackedColumnModel.setDataPoints(lowDataPoint);
		data.add(criticalStackedColumnModel);
		data.add(urgentStackedColumnModel);
		data.add(highStackedColumnModel);
		data.add(mediumStackedColumnModel);
		data.add(lowStackedColumnModel);
		return data;
	}

	public Result fetchTableHeaderDetails(int year){
		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Priority");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> criticalMap =  new HashMap<>();
		Map<String, String> urgentMap =  new HashMap<>();
		Map<String, String> highMap =  new HashMap<>();
		Map<String, String> mediumMap =  new HashMap<>();
		Map<String, String> lowMap =  new HashMap<>();

		Map<String, String> ctaskMap =  new HashMap<>();


		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		criticalMap.put("priority","Critical");
		urgentMap.put("priority","Urgent");
		highMap.put("priority","High");
		mediumMap.put("priority","Medium");
		lowMap.put("priority","Low");

		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
			criticalMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase())).count()+"");
			urgentMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase())).count()+"");
			highMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase())).count()+"");
			mediumMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase())).count()+"");
			lowMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase())).count()+"");
		});
		rowDetails.add(criticalMap);
		rowDetails.add(urgentMap);
		rowDetails.add(highMap);
		rowDetails.add(mediumMap);
		rowDetails.add(lowMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);
      return result;
	}


	public Result fetchTaskDetails(int year){
		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Tasks");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> ctaskMap =  new HashMap<>();
		Map<String, String> sctaskMap =  new HashMap<>();
		Map<String, String> ptaskMap =  new HashMap<>();
		Map<String, String> ritmMap =  new HashMap<>();
		Map<String, String> prbMap =  new HashMap<>();


		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		ctaskMap.put("priority","CTASK");
		sctaskMap.put("priority","SCTASK");
		ptaskMap.put("priority","PTASK");
		ritmMap.put("priority","RITM");
		prbMap.put("priority","PRB");


		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
			ctaskMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getStream().equals("CH & CTASK".toUpperCase())).count()+"");
			sctaskMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getStream().equals("SCTASK".toUpperCase())).count()+"");
			ptaskMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getStream().equals("PTASK".toUpperCase())).count()+"");
			ritmMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getStream().equals("RITM".toUpperCase())).count()+"");
			prbMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getStream().equals("PRB".toUpperCase())).count()+"");
		});
		rowDetails.add(ctaskMap);
		rowDetails.add(sctaskMap);
		rowDetails.add(ptaskMap);
		rowDetails.add(ritmMap);
		rowDetails.add(prbMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);
		return result;
	}

	public Result fetchOpenshiftTableDetails(int year){
		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Priority");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> criticalMap =  new HashMap<>();
		Map<String, String> urgentMap =  new HashMap<>();
		Map<String, String> highMap =  new HashMap<>();
		Map<String, String> mediumMap =  new HashMap<>();
		Map<String, String> lowMap =  new HashMap<>();


		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		criticalMap.put("priority","Critical");
		urgentMap.put("priority","Urgent");
		highMap.put("priority","High");
		mediumMap.put("priority","Medium");
		lowMap.put("priority","Low");





		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
			criticalMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase()) && obj.getStream().equals("IPIX".toUpperCase())).count()+"");
			urgentMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase()) && obj.getStream().equals("IPIX".toUpperCase())).count()+"");
			highMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase()) && obj.getStream().equals("IPIX".toUpperCase())).count()+"");
			mediumMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase()) && obj.getStream().equals("IPIX".toUpperCase())).count()+"");
			lowMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase()) && obj.getStream().equals("IPIX".toUpperCase())).count()+"");
		});
		rowDetails.add(criticalMap);
		rowDetails.add(urgentMap);
		rowDetails.add(highMap);
		rowDetails.add(mediumMap);
		rowDetails.add(lowMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);
		return result;
	}

	public Result fetchLandingTableDetails(int year){
		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Priority");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> criticalMap =  new HashMap<>();
		Map<String, String> urgentMap =  new HashMap<>();
		Map<String, String> highMap =  new HashMap<>();
		Map<String, String> mediumMap =  new HashMap<>();
		Map<String, String> lowMap =  new HashMap<>();


		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		criticalMap.put("priority","Critical");
		urgentMap.put("priority","Urgent");
		highMap.put("priority","High");
		mediumMap.put("priority","Medium");
		lowMap.put("priority","Low");





		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
			criticalMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase()) && obj.getStream().equals("LANDING".toUpperCase())).count()+"");
			urgentMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase()) && obj.getStream().equals("LANDING".toUpperCase())).count()+"");
			highMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase()) && obj.getStream().equals("LANDING".toUpperCase())).count()+"");
			mediumMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase()) && obj.getStream().equals("LANDING".toUpperCase())).count()+"");
			lowMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase()) && obj.getStream().equals("LANDING".toUpperCase())).count()+"");
		});
		rowDetails.add(criticalMap);
		rowDetails.add(urgentMap);
		rowDetails.add(highMap);
		rowDetails.add(mediumMap);
		rowDetails.add(lowMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);
		return result;
	}

	
	public Result fetchTableBatchesDetails(int year){
		
		
		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Priority");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> criticalMap =  new HashMap<>();
		Map<String, String> urgentMap =  new HashMap<>();
		Map<String, String> highMap =  new HashMap<>();
		Map<String, String> mediumMap =  new HashMap<>();
		Map<String, String> lowMap =  new HashMap<>();


		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		criticalMap.put("priority","Critical");
		urgentMap.put("priority","Urgent");
		highMap.put("priority","High");
		mediumMap.put("priority","Medium");
		lowMap.put("priority","Low");


		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);			
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), "CRC-BATCHES");
			criticalMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase())).count()+"");
			urgentMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase())).count()+"");
			highMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase())).count()+"");
			mediumMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase())).count()+"");
			lowMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase())).count()+"");

		});
		rowDetails.add(criticalMap);
		rowDetails.add(urgentMap);
		rowDetails.add(highMap);
		rowDetails.add(mediumMap);
		rowDetails.add(lowMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);

		return result;
	}

	
	public Result fetchTableIdrsDetails(int year){

		List<HeaderDetails> headerDetails = new ArrayList<>();
		HeaderDetails headerDetail = new HeaderDetails();
		headerDetail.setHeaderName("Priority");
		headerDetail.setField("priority");
		headerDetail.setPinned("left");
		headerDetails.add(headerDetail);

		AtomicInteger i= new AtomicInteger();

		List<Map<String, String>> rowDetails = new ArrayList<>();
		Map<String, String> criticalMap =  new HashMap<>();
		Map<String, String> urgentMap =  new HashMap<>();
		Map<String, String> highMap =  new HashMap<>();
		Map<String, String> mediumMap =  new HashMap<>();
		Map<String, String> lowMap =  new HashMap<>();



		List<Map<String,String>> taskIncidentList = new ArrayList<>();
		criticalMap.put("priority","Critical");
		urgentMap.put("priority","Urgent");
		highMap.put("priority","High");
		mediumMap.put("priority","Medium");
		lowMap.put("priority","Low");




		FridayAndThursdayDates.getWeeklyDays(year).stream().forEach(rec -> {
			HeaderDetails details = new HeaderDetails();
			details.setHeaderName(rec.getFromDate()+" - "+rec.getToDate());
			details.setField("W"+ (i.incrementAndGet()));
			headerDetails.add(details);			
			List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), "IDRS");
			criticalMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Critical".toUpperCase())).count()+"");
			urgentMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Urgent".toUpperCase())).count()+"");
			highMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("High".toUpperCase())).count()+"");
			mediumMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Medium".toUpperCase())).count()+"");
			lowMap.put("W"+i,slfReportDetails.stream().filter(obj -> obj.getPriority().equals("Low".toUpperCase())).count()+"");

		});
		rowDetails.add(criticalMap);
		rowDetails.add(urgentMap);
		rowDetails.add(highMap);
		rowDetails.add(mediumMap);
		rowDetails.add(lowMap);
		Result result = new Result();
		result.setColumnDef(headerDetails);
		result.setRowData(rowDetails);

		return result;
	}

}
