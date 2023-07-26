package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.slf.reports.ExcelFileReader;
import com.slf.reports.response.DataPointsModel;
import com.slf.reports.response.StackedColumnModel;
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
}
