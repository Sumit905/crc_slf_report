package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import com.slf.reports.entity.ReportDetails;
import org.springframework.web.multipart.MultipartFile;

public interface SlfReportService {

	List<ReportDetails> saveReportDetails(MultipartFile excel) throws ParseException, IOException;
	
	List<ReportDetails> fetchReportDetails();
	
	List<ReportDetails> fetchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate);
	
	List<ReportDetails> fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate,LocalDate toDate, String consumer);

	List<String> fetchSheetNames() throws ParseException, IOException;

}
