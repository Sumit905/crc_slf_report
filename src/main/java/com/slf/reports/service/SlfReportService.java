package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.entity.SheetNameEntity;
import com.slf.reports.response.Result;
import com.slf.reports.response.StackedColumnModel;
import org.springframework.web.multipart.MultipartFile;

public interface SlfReportService {

	List<ReportDetails> saveReportDetails(MultipartFile excel) throws ParseException, IOException;
	
	List<ReportDetails> fetchReportDetails();
	
	List<ReportDetails> fetchReportDetailsOnBasesOfDate(LocalDate fromDate,LocalDate toDate);
	
	List<ReportDetails> fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate,LocalDate toDate, String consumer);

	List<SheetNameEntity> fetchSheetNames() throws ParseException, IOException;

	List<String> fetchSheetNamesWithTask() throws ParseException, IOException;

	List<StackedColumnModel> getStackedColumnDetails(int year);

	Result fetchTableHeaderDetails(int year);
	
	Result fetchTableBatchesDetails(int year);
	
	Result fetchTableIdrsDetails(int year);

	Result fetchTaskDetails(int year);

	Result fetchOpenshiftTableDetails(int year);

	Result fetchLandingTableDetails(int year);

	Result fetchDateClarificationDetails(int year);
	Result fetchDateCorrectionDetails(int year);

}
