package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.slf.reports.ExcelFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.repository.SlfReportRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SlfReportServiceImpl implements SlfReportService {

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
}
