package com.slf.reports.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.slf.reports.entity.ReportDetails;

public interface SlfReportRepository extends CrudRepository<ReportDetails, Long> {

	List<ReportDetails> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

	@Query(value = "from ReportDetails t where date BETWEEN :startDate AND :endDate AND stream= :consumer")
	List<ReportDetails> findConsumerByDateBetween(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("consumer") String consumer);
}
