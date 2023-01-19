package com.slf.reports.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.slf.reports.entity.ReportDetails;

public interface SlfReportRepository extends CrudRepository<ReportDetails, Long>{
		
	
	List<ReportDetails> findAllByDateBetween(LocalDate startDate,
			LocalDate endDate)	;
}
