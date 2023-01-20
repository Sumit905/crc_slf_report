package com.slf.reports.service;

import java.time.LocalDate;
import java.util.List;

import com.slf.reports.entity.ReportDetails;

public interface SlfReportService {
	
	ReportDetails saveReportDetails(ReportDetails reportDetails);
	
	List<ReportDetails> fatchReportDetails();
	
	List<ReportDetails> fatchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate);

}