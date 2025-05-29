package com.slf.reports.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.repository.SlfReportRepository;

@Service
public class SlfReportServiceImpl implements SlfReportService {

	@Autowired
	private SlfReportRepository reportRepository;
	
	public ReportDetails saveReportDetails(ReportDetails reportDetails) {
		return reportRepository.save(reportDetails);
	}
	
	public List<ReportDetails> fatchReportDetails(){
		return (List<ReportDetails>) reportRepository.findAll();
	}
	
	
	public List<ReportDetails> fatchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate) {
		
		return reportRepository.findAllByDateBetween(fromDate,toDate);
	}

	@Override
	public List<ReportDetails> fatchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate, LocalDate toDate,
			String consumer) {
		
		return reportRepository.findConsumerByDateBetween(fromDate,toDate,consumer);
	}

	@Override
	public List<String> fetchAllStreamName() {
		return reportRepository.getAllStreamName();
	}
}
