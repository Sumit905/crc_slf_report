package com.slf.reports.service;

import java.time.LocalDate;
import java.util.List;

import com.slf.reports.entity.ReportDetails;

public interface SlfReportService {

	List<ReportDetails> saveReportDetails(List<ReportDetails> reportDetailsList);
	
	List<ReportDetails> fatchReportDetails();
	
	List<ReportDetails> fatchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate);
	
	List<ReportDetails> fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate,LocalDate toDate, String consumer);

}
