package com.slf.reports.service;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.repository.SlfReportRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlfReportServiceImpl implements SlfReportService {

	@Autowired
	private SlfReportRepository reportRepository;
	@Transactional
	public List<ReportDetails> saveReportDetails(List<ReportDetails> reportDetailsList) {
		return  convertIteratorToList(reportRepository.saveAll(reportDetailsList).iterator());
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

	public static <T> List<T> convertIteratorToList(Iterator<T> iterator) {
		Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
		return StreamSupport.stream(spliterator, false)
							.collect(Collectors.toList());
	}
}
