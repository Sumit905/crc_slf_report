package com.slf.reports.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.slf.reports.ExcelFileReader;
import com.slf.reports.entity.SheetNameEntity;
import com.slf.reports.repository.SheetNamesRepository;
import com.slf.reports.request.WeeklyRequestParam;
import com.slf.reports.response.DataPointsModel;
import com.slf.reports.response.HeaderDetails;
import com.slf.reports.response.ResponseModel;
import com.slf.reports.response.Result;
import com.slf.reports.response.StackedColumnModel;
import com.slf.reports.utils.FridayAndThursdayDates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slf.reports.entity.ReportDetails;
import com.slf.reports.repository.SlfReportRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.slf.reports.utils.ConstantUtil.BASELOADED_AGREEMENTS_FOR_RIX;
import static com.slf.reports.utils.ConstantUtil.CRC_BATCHES;
import static com.slf.reports.utils.ConstantUtil.CRITICAL;
import static com.slf.reports.utils.ConstantUtil.CTASK;
import static com.slf.reports.utils.ConstantUtil.DATA_CLARIFICATION;
import static com.slf.reports.utils.ConstantUtil.DATA_CORRECTION;
import static com.slf.reports.utils.ConstantUtil.DATA_MISSING;
import static com.slf.reports.utils.ConstantUtil.DATA_REPUSH;
import static com.slf.reports.utils.ConstantUtil.HIGH;
import static com.slf.reports.utils.ConstantUtil.IDRS;
import static com.slf.reports.utils.ConstantUtil.IPIX;
import static com.slf.reports.utils.ConstantUtil.LANDING;
import static com.slf.reports.utils.ConstantUtil.LEFT;
import static com.slf.reports.utils.ConstantUtil.LOW;
import static com.slf.reports.utils.ConstantUtil.MEDIUM;
import static com.slf.reports.utils.ConstantUtil.PPE_JOB_FAILURES;
import static com.slf.reports.utils.ConstantUtil.PRB;
import static com.slf.reports.utils.ConstantUtil.PRB_FOR_CRC;
import static com.slf.reports.utils.ConstantUtil.PRB_RAISED_BY_CRC;
import static com.slf.reports.utils.ConstantUtil.PRIORITY;
import static com.slf.reports.utils.ConstantUtil.PTASK;
import static com.slf.reports.utils.ConstantUtil.RITM;
import static com.slf.reports.utils.ConstantUtil.SCTASK;
import static com.slf.reports.utils.ConstantUtil.STACKED_COLUMN;
import static com.slf.reports.utils.ConstantUtil.URGENT;

@Service
class SlfReportServiceImpl implements SlfReportService {



    @Autowired
    private SlfReportRepository reportRepository;

    @Autowired
    private SheetNamesRepository sheetNamesRepository;

    @Autowired
    private ExcelFileReader excelFileReader;

    @Transactional
    public List<ReportDetails> saveReportDetails(MultipartFile excel) throws ParseException, IOException {

        excelFileReader.setExcel(excel);

        //Create and Save the Sheet Name in DB.
        for(String sheetName : excelFileReader.fetchSheetNames()){
            SheetNameEntity sheetNameEntityObj = new SheetNameEntity();
            sheetNameEntityObj.setStream(sheetName);
            // Not Save the duplicate Sheet Name In DB.
            try{
                if(ObjectUtils.isEmpty(sheetNamesRepository.findByStream(sheetName))){
                    sheetNamesRepository.save(sheetNameEntityObj);
                }
            }catch(Exception ex){
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }


        List<ReportDetails> reportDetailsList = excelFileReader.fetchExcelData();
        reportDetailsList.removeIf(rec -> rec.getIncidentNo() == null);
        reportDetailsList.removeIf(rec -> rec.getDate() == null);
        reportDetailsList.stream()
                         .filter(rec -> rec.getCategorization() == null)
                         .forEach(rec -> rec.setCategorization("JOB FAILURE"));

        reportDetailsList.stream().filter(rec -> rec.getPriority() == null).forEach(rec -> rec.setPriority("NA"));

        // Remove duplicate incident no. and not save the Incident record in DB.
        reportDetailsList.removeIf(rec -> reportRepository.findByIncidentNo(rec.getIncidentNo())!= null);

        return convertIteratorToList(reportRepository.saveAll(reportDetailsList).iterator());
    }

    public List<ReportDetails> fetchReportDetails() {
        return (List<ReportDetails>) reportRepository.findAll();
    }

    public List<ReportDetails> fetchReportDetailsOnBasesOfDate(LocalDate fromDate, LocalDate toDate) {
        return reportRepository.findAllByDateBetween(fromDate, toDate);
    }

    @Override
    public List<ReportDetails> fetchReportDetailsOnBasesOfDateAndConsumer(LocalDate fromDate,
                                                                          LocalDate toDate,
                                                                          String consumer) {

        return reportRepository.findConsumerByDateBetween(fromDate, toDate, consumer);
    }

    public static <T> List<T> convertIteratorToList(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).collect(Collectors.toList());
    }

    public List<SheetNameEntity> fetchSheetNames(){
        return (List<SheetNameEntity>) sheetNamesRepository.findAll();
    }


    public List<String> fetchSheetNamesWithTask() {
        List<String> sheetNames = fetchSheetNames().stream().map(SheetNameEntity::getStream).collect(Collectors.toList());
        List<String> removeSheetNames = new ArrayList<>();
        removeSheetNames.add(IPIX);
        removeSheetNames.add(LANDING);
        removeSheetNames.add(IDRS);
        removeSheetNames.add(CRC_BATCHES);
        removeSheetNames.add(PRB_FOR_CRC);
        removeSheetNames.add(PPE_JOB_FAILURES);
        removeSheetNames.add(PRB_RAISED_BY_CRC);
        removeSheetNames.add(SCTASK);
        removeSheetNames.add("CH & CTASK");
        removeSheetNames.add(PTASK);
        removeSheetNames.add(BASELOADED_AGREEMENTS_FOR_RIX);
        removeSheetNames.add(RITM);
        sheetNames.removeIf(removeSheetNames::contains);
        return sheetNames;
    }

    @Override
    public List<StackedColumnModel> getTotalNoOfIncChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
        criticalStackedColumnModel.setType(STACKED_COLUMN);
        criticalStackedColumnModel.setName(CRITICAL);
        criticalStackedColumnModel.setShowInLegend("true");
        criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
        StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
        urgentStackedColumnModel.setType(STACKED_COLUMN);
        urgentStackedColumnModel.setName(URGENT);
        urgentStackedColumnModel.setShowInLegend("true");
        urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
        StackedColumnModel highStackedColumnModel = new StackedColumnModel();
        highStackedColumnModel.setType(STACKED_COLUMN);
        highStackedColumnModel.setName(HIGH);
        highStackedColumnModel.setShowInLegend("true");
        highStackedColumnModel.setyValueFormatString("## High Incidents");

        StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
        mediumStackedColumnModel.setType(STACKED_COLUMN);
        mediumStackedColumnModel.setName(MEDIUM);
        mediumStackedColumnModel.setShowInLegend("true");
        mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

        StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
        lowStackedColumnModel.setType(STACKED_COLUMN);
        lowStackedColumnModel.setName(LOW);
        lowStackedColumnModel.setShowInLegend("true");
        lowStackedColumnModel.setyValueFormatString("## Low Incidents");

        List<DataPointsModel> criticalDataPoint = new ArrayList<>();
        List<DataPointsModel> urgentDataPoint = new ArrayList<>();
        List<DataPointsModel> highDataPoint = new ArrayList<>();
        List<DataPointsModel> mediumDataPoint = new ArrayList<>();
        List<DataPointsModel> lowDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            criticalDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            urgentDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            highDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            mediumDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
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

    @Override
    public List<StackedColumnModel> getTotalOpenShiftChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
        criticalStackedColumnModel.setType(STACKED_COLUMN);
        criticalStackedColumnModel.setName(CRITICAL);
        criticalStackedColumnModel.setShowInLegend("true");
        criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
        StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
        urgentStackedColumnModel.setType(STACKED_COLUMN);
        urgentStackedColumnModel.setName(URGENT);
        urgentStackedColumnModel.setShowInLegend("true");
        urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
        StackedColumnModel highStackedColumnModel = new StackedColumnModel();
        highStackedColumnModel.setType(STACKED_COLUMN);
        highStackedColumnModel.setName(HIGH);
        highStackedColumnModel.setShowInLegend("true");
        highStackedColumnModel.setyValueFormatString("## High Incidents");

        StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
        mediumStackedColumnModel.setType(STACKED_COLUMN);
        mediumStackedColumnModel.setName(MEDIUM);
        mediumStackedColumnModel.setShowInLegend("true");
        mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

        StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
        lowStackedColumnModel.setType(STACKED_COLUMN);
        lowStackedColumnModel.setName(LOW);
        lowStackedColumnModel.setShowInLegend("true");
        lowStackedColumnModel.setyValueFormatString("## Low Incidents");

        List<DataPointsModel> criticalDataPoint = new ArrayList<>();
        List<DataPointsModel> urgentDataPoint = new ArrayList<>();
        List<DataPointsModel> highDataPoint = new ArrayList<>();
        List<DataPointsModel> mediumDataPoint = new ArrayList<>();
        List<DataPointsModel> lowDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(),IPIX);
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            criticalDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            urgentDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            highDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            mediumDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
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


    @Override
    public List<StackedColumnModel> getTotalLandingChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
        criticalStackedColumnModel.setType(STACKED_COLUMN);
        criticalStackedColumnModel.setName(CRITICAL);
        criticalStackedColumnModel.setShowInLegend("true");
        criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
        StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
        urgentStackedColumnModel.setType(STACKED_COLUMN);
        urgentStackedColumnModel.setName(URGENT);
        urgentStackedColumnModel.setShowInLegend("true");
        urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
        StackedColumnModel highStackedColumnModel = new StackedColumnModel();
        highStackedColumnModel.setType(STACKED_COLUMN);
        highStackedColumnModel.setName(HIGH);
        highStackedColumnModel.setShowInLegend("true");
        highStackedColumnModel.setyValueFormatString("## High Incidents");

        StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
        mediumStackedColumnModel.setType(STACKED_COLUMN);
        mediumStackedColumnModel.setName(MEDIUM);
        mediumStackedColumnModel.setShowInLegend("true");
        mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

        StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
        lowStackedColumnModel.setType(STACKED_COLUMN);
        lowStackedColumnModel.setName(LOW);
        lowStackedColumnModel.setShowInLegend("true");
        lowStackedColumnModel.setyValueFormatString("## Low Incidents");

        List<DataPointsModel> criticalDataPoint = new ArrayList<>();
        List<DataPointsModel> urgentDataPoint = new ArrayList<>();
        List<DataPointsModel> highDataPoint = new ArrayList<>();
        List<DataPointsModel> mediumDataPoint = new ArrayList<>();
        List<DataPointsModel> lowDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(),LANDING);
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            criticalDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            urgentDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            highDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            mediumDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
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

    @Override
    public List<StackedColumnModel> getTotalBatchesChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
        criticalStackedColumnModel.setType(STACKED_COLUMN);
        criticalStackedColumnModel.setName(CRITICAL);
        criticalStackedColumnModel.setShowInLegend("true");
        criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
        StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
        urgentStackedColumnModel.setType(STACKED_COLUMN);
        urgentStackedColumnModel.setName(URGENT);
        urgentStackedColumnModel.setShowInLegend("true");
        urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
        StackedColumnModel highStackedColumnModel = new StackedColumnModel();
        highStackedColumnModel.setType(STACKED_COLUMN);
        highStackedColumnModel.setName(HIGH);
        highStackedColumnModel.setShowInLegend("true");
        highStackedColumnModel.setyValueFormatString("## High Incidents");

        StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
        mediumStackedColumnModel.setType(STACKED_COLUMN);
        mediumStackedColumnModel.setName(MEDIUM);
        mediumStackedColumnModel.setShowInLegend("true");
        mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

        StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
        lowStackedColumnModel.setType(STACKED_COLUMN);
        lowStackedColumnModel.setName(LOW);
        lowStackedColumnModel.setShowInLegend("true");
        lowStackedColumnModel.setyValueFormatString("## Low Incidents");

        List<DataPointsModel> criticalDataPoint = new ArrayList<>();
        List<DataPointsModel> urgentDataPoint = new ArrayList<>();
        List<DataPointsModel> highDataPoint = new ArrayList<>();
        List<DataPointsModel> mediumDataPoint = new ArrayList<>();
        List<DataPointsModel> lowDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(),CRC_BATCHES);
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            criticalDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            urgentDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            highDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            mediumDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
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

    @Override
    public List<StackedColumnModel> getTotalIdresChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel criticalStackedColumnModel = new StackedColumnModel();
        criticalStackedColumnModel.setType(STACKED_COLUMN);
        criticalStackedColumnModel.setName(CRITICAL);
        criticalStackedColumnModel.setShowInLegend("true");
        criticalStackedColumnModel.setyValueFormatString("## Critical Incidents");
        StackedColumnModel urgentStackedColumnModel = new StackedColumnModel();
        urgentStackedColumnModel.setType(STACKED_COLUMN);
        urgentStackedColumnModel.setName(URGENT);
        urgentStackedColumnModel.setShowInLegend("true");
        urgentStackedColumnModel.setyValueFormatString("## Urgent Incidents");
        StackedColumnModel highStackedColumnModel = new StackedColumnModel();
        highStackedColumnModel.setType(STACKED_COLUMN);
        highStackedColumnModel.setName(HIGH);
        highStackedColumnModel.setShowInLegend("true");
        highStackedColumnModel.setyValueFormatString("## High Incidents");

        StackedColumnModel mediumStackedColumnModel = new StackedColumnModel();
        mediumStackedColumnModel.setType(STACKED_COLUMN);
        mediumStackedColumnModel.setName(MEDIUM);
        mediumStackedColumnModel.setShowInLegend("true");
        mediumStackedColumnModel.setyValueFormatString("## Medium Incidents");

        StackedColumnModel lowStackedColumnModel = new StackedColumnModel();
        lowStackedColumnModel.setType(STACKED_COLUMN);
        lowStackedColumnModel.setName(LOW);
        lowStackedColumnModel.setShowInLegend("true");
        lowStackedColumnModel.setyValueFormatString("## Low Incidents");

        List<DataPointsModel> criticalDataPoint = new ArrayList<>();
        List<DataPointsModel> urgentDataPoint = new ArrayList<>();
        List<DataPointsModel> highDataPoint = new ArrayList<>();
        List<DataPointsModel> mediumDataPoint = new ArrayList<>();
        List<DataPointsModel> lowDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(),IDRS);
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            criticalDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            urgentDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            highDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            mediumDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
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
    @Override
    public List<StackedColumnModel> getTaskIncChartDetails(int year) {

        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel ctaskStackedColumnModel = new StackedColumnModel();
        ctaskStackedColumnModel.setType(STACKED_COLUMN);
        ctaskStackedColumnModel.setName(CTASK);
        ctaskStackedColumnModel.setShowInLegend("true");
        ctaskStackedColumnModel.setyValueFormatString("## "+CTASK+" Incidents");

        StackedColumnModel sctaskStackedColumnModel = new StackedColumnModel();
        sctaskStackedColumnModel.setType(STACKED_COLUMN);
        sctaskStackedColumnModel.setName(SCTASK);
        sctaskStackedColumnModel.setShowInLegend("true");
        sctaskStackedColumnModel.setyValueFormatString("## "+SCTASK+" Incidents");

        StackedColumnModel ptaskStackedColumnModel = new StackedColumnModel();
        ptaskStackedColumnModel.setType(STACKED_COLUMN);
        ptaskStackedColumnModel.setName(PTASK);
        ptaskStackedColumnModel.setShowInLegend("true");
        ptaskStackedColumnModel.setyValueFormatString("## "+PTASK+" Incidents");

        StackedColumnModel ritmStackedColumnModel = new StackedColumnModel();
        ritmStackedColumnModel.setType(STACKED_COLUMN);
        ritmStackedColumnModel.setName(RITM);
        ritmStackedColumnModel.setShowInLegend("true");
        ritmStackedColumnModel.setyValueFormatString("## "+RITM+" Incidents");

        StackedColumnModel prbStackedColumnModel = new StackedColumnModel();
        prbStackedColumnModel.setType(STACKED_COLUMN);
        prbStackedColumnModel.setName(PRB);
        prbStackedColumnModel.setShowInLegend("true");
        prbStackedColumnModel.setyValueFormatString("## "+PRB+" Incidents");

        List<DataPointsModel> ctaskDataPoint = new ArrayList<>();
        List<DataPointsModel> sctaskDataPoint = new ArrayList<>();
        List<DataPointsModel> ptaskDataPoint = new ArrayList<>();
        List<DataPointsModel> ritmDataPoint = new ArrayList<>();
        List<DataPointsModel> prbDataPoint = new ArrayList<>();

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals("CH & CTASK".toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            ctaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(SCTASK.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            sctaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(PTASK.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            ptaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(RITM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            ritmDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(PRB.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(rec.getFromDate() + " - " + rec.getToDate());
            prbDataPoint.add(dataPointsModel);
        });
        ctaskStackedColumnModel.setDataPoints(ctaskDataPoint);
        sctaskStackedColumnModel.setDataPoints(sctaskDataPoint);
        ptaskStackedColumnModel.setDataPoints(ptaskDataPoint);
        ritmStackedColumnModel.setDataPoints(ritmDataPoint);
        prbStackedColumnModel.setDataPoints(prbDataPoint);
        data.add(ctaskStackedColumnModel);
        data.add(sctaskStackedColumnModel);
        data.add(ptaskStackedColumnModel);
        data.add(ritmStackedColumnModel);
        data.add(prbStackedColumnModel);
        return data;
    }


    private static List<HeaderDetails> getHeaderDetails(String columnName, int year){
        List<HeaderDetails> headerDetails = new ArrayList<>();
        HeaderDetails headerDetail = new HeaderDetails();
        headerDetail.setHeaderName(columnName);
        headerDetail.setField(PRIORITY);
        headerDetail.setPinned(LEFT);
        headerDetails.add(headerDetail);
        AtomicInteger i = new AtomicInteger();
        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            HeaderDetails details = new HeaderDetails();
            details.setHeaderName(rec.getFromDate() + " - " + rec.getToDate());
            details.setField("W" + (i.incrementAndGet()));
            headerDetails.add(details);
        });
        return headerDetails;
    }
    public Result fetchTableHeaderDetails(int year) {
        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Priority",year);
        AtomicInteger i = new AtomicInteger();
        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> criticalMap = new HashMap<>();
        Map<String, String> urgentMap = new HashMap<>();
        Map<String, String> highMap = new HashMap<>();
        Map<String, String> mediumMap = new HashMap<>();
        Map<String, String> lowMap = new HashMap<>();
        criticalMap.put(PRIORITY, CRITICAL);
        urgentMap.put(PRIORITY, URGENT);
        highMap.put(PRIORITY, HIGH);
        mediumMap.put(PRIORITY, MEDIUM);
        lowMap.put(PRIORITY, LOW);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            criticalMap.put("W" + i,
                            slfReportDetails.stream()
                                            .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                            .count() + "");
            urgentMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                          .count() + "");
            highMap.put("W" + i,
                        slfReportDetails.stream().filter(obj -> obj.getPriority().equals(HIGH.toUpperCase())).count()
                        + "");
            mediumMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                          .count() + "");
            lowMap.put("W" + i,
                       slfReportDetails.stream().filter(obj -> obj.getPriority().equals(LOW.toUpperCase())).count()
                       + "");
        });
        rowDetails.add(criticalMap);
        rowDetails.add(urgentMap);
        rowDetails.add(highMap);
        rowDetails.add(mediumMap);
        rowDetails.add(lowMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Result fetchTaskDetails(int year) {
        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Tasks",year);
        AtomicInteger i = new AtomicInteger();
        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> ctaskMap = new HashMap<>();
        Map<String, String> sctaskMap = new HashMap<>();
        Map<String, String> ptaskMap = new HashMap<>();
        Map<String, String> ritmMap = new HashMap<>();
        Map<String, String> prbMap = new HashMap<>();
        ctaskMap.put(PRIORITY, CTASK);
        sctaskMap.put(PRIORITY, SCTASK);
        ptaskMap.put(PRIORITY, PTASK);
        ritmMap.put(PRIORITY, RITM);
        prbMap.put(PRIORITY, PRB);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            ctaskMap.put("W" + i,
                         slfReportDetails.stream()
                                         .filter(obj -> obj.getStream().equals("CH & CTASK".toUpperCase()))
                                         .count() + "");
            sctaskMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getStream().equals(SCTASK.toUpperCase()))
                                          .count() + "");
            ptaskMap.put("W" + i,
                         slfReportDetails.stream().filter(obj -> obj.getStream().equals(PTASK.toUpperCase())).count()
                         + "");
            ritmMap.put("W" + i,
                        slfReportDetails.stream().filter(obj -> obj.getStream().equals(RITM.toUpperCase())).count()
                        + "");
            prbMap.put("W" + i,
                       slfReportDetails.stream().filter(obj -> obj.getStream().equals(PRB.toUpperCase())).count()
                       + "");
        });
        rowDetails.add(ctaskMap);
        rowDetails.add(sctaskMap);
        rowDetails.add(ptaskMap);
        rowDetails.add(ritmMap);
        rowDetails.add(prbMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Result fetchOpenshiftTableDetails(int year) {
        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Priority",year);
        AtomicInteger i = new AtomicInteger();
        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> criticalMap = new HashMap<>();
        Map<String, String> urgentMap = new HashMap<>();
        Map<String, String> highMap = new HashMap<>();
        Map<String, String> mediumMap = new HashMap<>();
        Map<String, String> lowMap = new HashMap<>();
        criticalMap.put(PRIORITY, CRITICAL);
        urgentMap.put(PRIORITY, URGENT);
        highMap.put(PRIORITY, HIGH);
        mediumMap.put(PRIORITY, MEDIUM);
        lowMap.put(PRIORITY, LOW);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            criticalMap.put("W" + i,
                            slfReportDetails.stream()
                                            .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase())
                                                           && obj.getStream().equals(IPIX.toUpperCase()))
                                            .count() + "");
            urgentMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase())
                                                         && obj.getStream().equals(IPIX.toUpperCase()))
                                          .count() + "");
            highMap.put("W" + i,
                        slfReportDetails.stream()
                                        .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()) && obj.getStream()
                                                                                                            .equals(IPIX.toUpperCase()))
                                        .count() + "");
            mediumMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase())
                                                         && obj.getStream().equals(IPIX.toUpperCase()))
                                          .count() + "");
            lowMap.put("W" + i,
                       slfReportDetails.stream()
                                       .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()) && obj.getStream()
                                                                                                          .equals(IPIX.toUpperCase()))
                                       .count() + "");
        });
        rowDetails.add(criticalMap);
        rowDetails.add(urgentMap);
        rowDetails.add(highMap);
        rowDetails.add(mediumMap);
        rowDetails.add(lowMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Result fetchLandingTableDetails(int year) {
        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Priority",year);
        AtomicInteger i = new AtomicInteger();
        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> criticalMap = new HashMap<>();
        Map<String, String> urgentMap = new HashMap<>();
        Map<String, String> highMap = new HashMap<>();
        Map<String, String> mediumMap = new HashMap<>();
        Map<String, String> lowMap = new HashMap<>();
        criticalMap.put(PRIORITY, CRITICAL);
        urgentMap.put(PRIORITY, URGENT);
        highMap.put(PRIORITY, HIGH);
        mediumMap.put(PRIORITY, MEDIUM);
        lowMap.put(PRIORITY, LOW);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(rec.getFromDate(), rec.getToDate());
            criticalMap.put("W" + i,
                            slfReportDetails.stream()
                                            .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase())
                                                           && obj.getStream().equals(LANDING.toUpperCase()))
                                            .count() + "");
            urgentMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase())
                                                         && obj.getStream().equals(LANDING.toUpperCase()))
                                          .count() + "");
            highMap.put("W" + i,
                        slfReportDetails.stream()
                                        .filter(obj -> obj.getPriority().equals(HIGH.toUpperCase()) && obj.getStream()
                                                                                                            .equals(LANDING.toUpperCase()))
                                        .count() + "");
            mediumMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase())
                                                         && obj.getStream().equals(LANDING.toUpperCase()))
                                          .count() + "");
            lowMap.put("W" + i,
                       slfReportDetails.stream()
                                       .filter(obj -> obj.getPriority().equals(LOW.toUpperCase()) && obj.getStream()
                                                                                                          .equals(LANDING.toUpperCase()))
                                       .count() + "");
        });
        rowDetails.add(criticalMap);
        rowDetails.add(urgentMap);
        rowDetails.add(highMap);
        rowDetails.add(mediumMap);
        rowDetails.add(lowMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Result fetchTableBatchesDetails(int year) {

        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Priority",year);
        AtomicInteger i = new AtomicInteger();

        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> criticalMap = new HashMap<>();
        Map<String, String> urgentMap = new HashMap<>();
        Map<String, String> highMap = new HashMap<>();
        Map<String, String> mediumMap = new HashMap<>();
        Map<String, String> lowMap = new HashMap<>();
        criticalMap.put(PRIORITY, CRITICAL);
        urgentMap.put(PRIORITY, URGENT);
        highMap.put(PRIORITY, HIGH);
        mediumMap.put(PRIORITY, MEDIUM);
        lowMap.put(PRIORITY, LOW);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails =
                    fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), CRC_BATCHES);
            criticalMap.put("W" + i,
                            slfReportDetails.stream()
                                            .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                            .count() + "");
            urgentMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                          .count() + "");
            highMap.put("W" + i,
                        slfReportDetails.stream().filter(obj -> obj.getPriority().equals(HIGH.toUpperCase())).count()
                        + "");
            mediumMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                          .count() + "");
            lowMap.put("W" + i,
                       slfReportDetails.stream().filter(obj -> obj.getPriority().equals(LOW.toUpperCase())).count()
                       + "");
        });
        rowDetails.add(criticalMap);
        rowDetails.add(urgentMap);
        rowDetails.add(highMap);
        rowDetails.add(mediumMap);
        rowDetails.add(lowMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);

        return result;
    }

    public Result fetchTableIdrsDetails(int year) {

        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Priority",year);
        AtomicInteger i = new AtomicInteger();

        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String, String> criticalMap = new HashMap<>();
        Map<String, String> urgentMap = new HashMap<>();
        Map<String, String> highMap = new HashMap<>();
        Map<String, String> mediumMap = new HashMap<>();
        Map<String, String> lowMap = new HashMap<>();
        criticalMap.put(PRIORITY, CRITICAL);
        urgentMap.put(PRIORITY, URGENT);
        highMap.put(PRIORITY, HIGH);
        mediumMap.put(PRIORITY, MEDIUM);
        lowMap.put(PRIORITY, LOW);

        FridayAndThursdayDates.getWeeklyDays(year).forEach(rec -> {
            i.incrementAndGet();
            List<ReportDetails> slfReportDetails =
                    fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), IDRS);
            criticalMap.put("W" + i,
                            slfReportDetails.stream()
                                            .filter(obj -> obj.getPriority().equals(CRITICAL.toUpperCase()))
                                            .count() + "");
            urgentMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(URGENT.toUpperCase()))
                                          .count() + "");
            highMap.put("W" + i,
                        slfReportDetails.stream().filter(obj -> obj.getPriority().equals(HIGH.toUpperCase())).count()
                        + "");
            mediumMap.put("W" + i,
                          slfReportDetails.stream()
                                          .filter(obj -> obj.getPriority().equals(MEDIUM.toUpperCase()))
                                          .count() + "");
            lowMap.put("W" + i,
                       slfReportDetails.stream().filter(obj -> obj.getPriority().equals(LOW.toUpperCase())).count()
                       + "");
        });
        rowDetails.add(criticalMap);
        rowDetails.add(urgentMap);
        rowDetails.add(highMap);
        rowDetails.add(mediumMap);
        rowDetails.add(lowMap);
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);

        return result;
    }

    public Result fetchDateClarificationDetails(int year) {

        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Consumer",year);
        List<Map<String, String>> rowDetails = new ArrayList<>();
        List<String> sheetNames = fetchSheetNamesWithTask();
        List<WeeklyRequestParam> weeklyRequestParams = FridayAndThursdayDates.getWeeklyDays(year);
        sheetNames.forEach(sheetName -> {
            Map<String, String> consumerMap = new HashMap<>();
            consumerMap.put(PRIORITY, sheetName);
            AtomicInteger i = new AtomicInteger();
            weeklyRequestParams.forEach(rec -> {
                List<ReportDetails> slfReportDetails =
                        fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), sheetName);
                consumerMap.put("W" + i.incrementAndGet(),
                                String.valueOf(slfReportDetails.stream()
                                                               .filter(obj -> obj.getCategorization()
                                                                                 .equals(DATA_CLARIFICATION))
                                                               .count()));
            });
            rowDetails.add(consumerMap);
        });
        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Result fetchDateCorrectionDetails(int year) {

        List<HeaderDetails> headerDetails = SlfReportServiceImpl.getHeaderDetails("Consumer",year);
        List<WeeklyRequestParam> weeklyRequestParams = FridayAndThursdayDates.getWeeklyDays(year);
        List<Map<String, String>> rowDetails = new ArrayList<>();
        List<String> sheetNames = fetchSheetNamesWithTask();
        for(String sheetName : sheetNames){
            Map<String, String> consumerMap = new HashMap<>();
            consumerMap.put(PRIORITY, sheetName);
            AtomicInteger i = new AtomicInteger();
            weeklyRequestParams.forEach(rec -> {
                List<ReportDetails> slfReportDetails =
                        fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), sheetName);
                consumerMap.put("W" + i.incrementAndGet(),
                                String.valueOf(slfReportDetails.stream()
                                                               .filter(obj -> obj.getCategorization()
                                                                                 .equals(DATA_CORRECTION) || obj.getCategorization()
                                                                                                                .equals(DATA_REPUSH) || obj.getCategorization()
                                                                                                                                           .equals(DATA_MISSING))
                                                               .count()));
            });
            rowDetails.add(consumerMap);
        }

        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    @Override
    public Result fetchDetailsOfInc(String tab, String priority, String columnId, int year) {
        List<WeeklyRequestParam> weeklyRequestParams = FridayAndThursdayDates.getWeeklyDays(year);
        WeeklyRequestParam weeklyResult = weeklyRequestParams.stream().filter(rec -> rec.getWeeklyId().equals(columnId)).findFirst().orElse(null);
        List<ReportDetails> slfReportDetails;
        List<Map<String, String>> rowDetails= new ArrayList<>();
        Map<String, String> tabMap = new HashMap<>();
        tabMap.put("openshift-details",IPIX);
        tabMap.put("landing-details",LANDING);
        tabMap.put("batch-details",CRC_BATCHES);
        tabMap.put("idrs-details",IDRS);
        Map<String, String> tabDetailsMap = new HashMap<>();
        tabDetailsMap.put("data-clarification-details",DATA_CLARIFICATION);
        tabDetailsMap.put("data-correction-details",DATA_CORRECTION);
        if(tab.equals("total-incident")){
            slfReportDetails = fetchReportDetailsOnBasesOfDate(weeklyResult.getFromDate(), weeklyResult.getToDate());
            slfReportDetails.stream().filter(rec-> rec.getPriority().equalsIgnoreCase(priority)).forEach(reportDetails ->{
                Map<String, String> mapDetails = new HashMap<>();
                mapDetails.put("inc",reportDetails.getIncidentNo());
                mapDetails.put("notes",reportDetails.getWorkNotes());
                mapDetails.put("date",reportDetails.getDate()+"");
                mapDetails.put("priority",reportDetails.getPriority());
                rowDetails.add(mapDetails);
            });

        }else if(tab.equals("task-details")){

            slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyResult.getFromDate(),weeklyResult.getToDate(),CTASK.equalsIgnoreCase(priority)?"CH & CTASK":priority);
            slfReportDetails.forEach(reportDetails ->{
                Map<String, String> mapDetails = new HashMap<>();
                mapDetails.put("inc",reportDetails.getIncidentNo());
                mapDetails.put("notes",reportDetails.getWorkNotes());
                mapDetails.put("date",reportDetails.getDate()+"");
                mapDetails.put("priority",reportDetails.getPriority());
                rowDetails.add(mapDetails);
            });
        } else if(tabMap.containsKey(tab)){
            slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyResult.getFromDate(),weeklyResult.getToDate(),tabMap.get(tab));
            slfReportDetails.forEach(reportDetails ->{
                Map<String, String> mapDetails = new HashMap<>();
                mapDetails.put("inc",reportDetails.getIncidentNo());
                mapDetails.put("notes",reportDetails.getWorkNotes());
                mapDetails.put("date",reportDetails.getDate()+"");
                mapDetails.put("priority",reportDetails.getPriority());
                rowDetails.add(mapDetails);
            });
        } else if(tabDetailsMap.containsKey(tab)){
            slfReportDetails = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyResult.getFromDate(),weeklyResult.getToDate(),priority);
            if(tabDetailsMap.get(tab).equals(DATA_CORRECTION)){
                slfReportDetails.stream().filter(rec ->rec.getCategorization().equals(DATA_CORRECTION) || rec.getCategorization().equals(DATA_REPUSH) || rec.getCategorization().equals(DATA_MISSING.toUpperCase())).forEach(reportDetails ->{
                    Map<String, String> mapDetails = new HashMap<>();
                    mapDetails.put("inc",reportDetails.getIncidentNo());
                    mapDetails.put("notes",reportDetails.getWorkNotes());
                    mapDetails.put("date",reportDetails.getDate()+"");
                    mapDetails.put("priority",reportDetails.getPriority());
                    rowDetails.add(mapDetails);
                });
            } else {
                slfReportDetails.stream().filter(rec-> rec.getCategorization().equalsIgnoreCase(tabDetailsMap.get(tab))).forEach(reportDetails ->{
                    Map<String, String> mapDetails = new HashMap<>();
                    mapDetails.put("inc",reportDetails.getIncidentNo());
                    mapDetails.put("notes",reportDetails.getWorkNotes());
                    mapDetails.put("date",reportDetails.getDate()+"");
                    mapDetails.put("priority",reportDetails.getPriority());
                    rowDetails.add(mapDetails);
                });
            }

        }

        List<HeaderDetails> headerDetails = new ArrayList<>();
        HeaderDetails details = new HeaderDetails();
        details.setHeaderName("Date");
        details.setField("date");
        details.setFlex(1);
        headerDetails.add(details);
        details = new HeaderDetails();
        details.setHeaderName("Incident No.");
        details.setField("inc");
        details.setFlex(1);
        headerDetails.add(details);
        details = new HeaderDetails();
        details.setHeaderName("Work Notes");
        details.setField("notes");
        details.setTooltipField("notes");
        details.setWidth(150);
        details.setSuppressSizeToFit(true);
        details.setFlex(3);
        headerDetails.add(details);
        details = new HeaderDetails();
        details.setHeaderName("Priority");
        details.setField("priority");
        details.setFlex(1);
        headerDetails.add(details);

        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;
    }

    public Map<String, ResponseModel> fetchResponseModels(int year){
        Map<String,ResponseModel>  responseModelMap = new LinkedHashMap<>();
        FridayAndThursdayDates.getWeeklyDays(year).forEach(weeklyDate -> {

            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(weeklyDate.getFromDate(), weeklyDate.getToDate());
            ResponseModel responseModel = new ResponseModel();

            Map<String,Long> incident = new HashMap<>();
            incident.put(CRITICAL,slfReportDetails.stream().filter(rec -> rec.getPriority().equals(CRITICAL.toUpperCase())).count());
            incident.put(URGENT,slfReportDetails.stream().filter(rec -> rec.getPriority().equals(URGENT.toUpperCase())).count());
            incident.put(HIGH,slfReportDetails.stream().filter(rec -> rec.getPriority().equals(HIGH.toUpperCase())).count());
            incident.put(MEDIUM,slfReportDetails.stream().filter(rec -> rec.getPriority().equals(MEDIUM.toUpperCase())).count());
            incident.put(LOW,slfReportDetails.stream().filter(rec -> rec.getPriority().equals(LOW.toUpperCase())).count());
            responseModel.setTotalNoOfIncident(incident);


            Map<String,Long> taskIncident = new HashMap<>();
            taskIncident.put(CTASK,slfReportDetails.stream().filter(rec -> rec.getStream().equals("CH & CTASK")).count());
            taskIncident.put(SCTASK,slfReportDetails.stream().filter(rec -> rec.getStream().equals(SCTASK)).count());
            taskIncident.put(PTASK,slfReportDetails.stream().filter(rec -> rec.getStream().equals(PTASK)).count());
            taskIncident.put(RITM,slfReportDetails.stream().filter(rec -> rec.getStream().equals(RITM)).count());
            taskIncident.put(PRB,slfReportDetails.stream().filter(rec -> rec.getStream().equals(PRB)).count());
            responseModel.setTaskIncident(taskIncident);

            List<ReportDetails> slfReportDetailsLanding = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), LANDING);
            Map<String,Long> landingIncident = new HashMap<>();
            landingIncident.put(CRITICAL,slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals(CRITICAL.toUpperCase())).count());
            landingIncident.put(URGENT,slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals(URGENT.toUpperCase())).count());
            landingIncident.put(HIGH,slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals(HIGH.toUpperCase())).count());
            landingIncident.put(MEDIUM,slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals(MEDIUM.toUpperCase())).count());
            landingIncident.put(LOW,slfReportDetailsLanding.stream().filter(rec -> rec.getPriority().equals(LOW.toUpperCase())).count());
            responseModel.setLandingIncident(landingIncident);

            List<ReportDetails> slfReportDetailsIdrs = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), IDRS);
            Map<String,Long> idrsIncident = new HashMap<>();
            slfReportDetailsIdrs.stream().filter(rec -> rec.getPriority().equalsIgnoreCase(HIGH.toUpperCase())).forEach(
                    System.out::println);

            idrsIncident.put(CRITICAL,calculateIncidentNo(slfReportDetailsIdrs,CRITICAL.toUpperCase()));
            idrsIncident.put(URGENT,calculateIncidentNo(slfReportDetailsIdrs,URGENT.toUpperCase()));
            idrsIncident.put(HIGH,calculateIncidentNo(slfReportDetailsIdrs,HIGH.toUpperCase()));
            idrsIncident.put(MEDIUM,calculateIncidentNo(slfReportDetailsIdrs,MEDIUM.toUpperCase()));
            idrsIncident.put(LOW,calculateIncidentNo(slfReportDetailsIdrs,LOW.toUpperCase()));
            responseModel.setIdsIncident(idrsIncident);

            List<ReportDetails> slfReportDetailsBatchs = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), CRC_BATCHES);
            Map<String,Long> batchesIncident = new HashMap<>();
            batchesIncident.put(CRITICAL,calculateIncidentNo(slfReportDetailsBatchs,CRITICAL.toUpperCase()));
            batchesIncident.put(URGENT,calculateIncidentNo(slfReportDetailsBatchs,URGENT.toUpperCase()));
            batchesIncident.put(HIGH,calculateIncidentNo(slfReportDetailsBatchs,HIGH.toUpperCase()));
            batchesIncident.put(MEDIUM,calculateIncidentNo(slfReportDetailsBatchs,MEDIUM.toUpperCase()));
            batchesIncident.put(LOW,calculateIncidentNo(slfReportDetailsBatchs,LOW.toUpperCase()));
            responseModel.setBatchesIncident(batchesIncident);

            List<ReportDetails> slfReportDetailsOpen = fetchReportDetailsOnBasesOfDateAndConsumer(weeklyDate.getFromDate(),weeklyDate.getToDate(), IPIX);
            Map<String,Long> openShiftIncident = new HashMap<>();
            openShiftIncident.put(CRITICAL,calculateIncidentNo(slfReportDetailsOpen,CRITICAL.toUpperCase()));
            openShiftIncident.put(URGENT,calculateIncidentNo(slfReportDetailsOpen,URGENT.toUpperCase()));
            openShiftIncident.put(HIGH,calculateIncidentNo(slfReportDetailsOpen,HIGH.toUpperCase()));
            openShiftIncident.put(MEDIUM,calculateIncidentNo(slfReportDetailsOpen,MEDIUM.toUpperCase()));
            openShiftIncident.put(LOW,calculateIncidentNo(slfReportDetailsOpen,LOW.toUpperCase()));
            responseModel.setOpenShiftIncident(openShiftIncident);

            Map<String,Long> dataClarificationIncident = new HashMap<>();
            fetchSheetNamesWithTask().forEach(sheetName -> dataClarificationIncident.put(sheetName,slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.toUpperCase())).filter(rec -> rec.getCategorization().equals(DATA_CLARIFICATION)).count()));
            responseModel.setDataClarificationIncident(dataClarificationIncident);

            Map<String,Long> dataCorrectionIncident = new HashMap<>();
            fetchSheetNamesWithTask().forEach(sheetName ->
                                                                       dataCorrectionIncident.put(sheetName,slfReportDetails.stream().filter(rec -> rec.getStream().equals(sheetName.toUpperCase())).filter(rec -> rec.getCategorization().equals(DATA_CORRECTION) || rec.getCategorization().equals(DATA_REPUSH) || rec.getCategorization().equals(DATA_MISSING.toUpperCase())).count()));
            responseModel.setDataCorrectionIncident(dataCorrectionIncident);
            responseModelMap.put(weeklyDate.getFromDate()+" - "+weeklyDate.getToDate(),responseModel);
        });

        return responseModelMap;
    }

    public List<StackedColumnModel> fetchDetailsOfIncident(LocalDate fromDate,LocalDate toDate){
        List<String> sheetNames = fetchSheetNames().stream().map(SheetNameEntity::getStream).collect(Collectors.toList());
        List<StackedColumnModel> data = new ArrayList<>();
        StackedColumnModel ctaskStackedColumnModel = new StackedColumnModel();
        ctaskStackedColumnModel.setType(STACKED_COLUMN);
        ctaskStackedColumnModel.setName(CTASK);
        ctaskStackedColumnModel.setShowInLegend("true");
        ctaskStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel sctaskStackedColumnModel = new StackedColumnModel();
        sctaskStackedColumnModel.setType(STACKED_COLUMN);
        sctaskStackedColumnModel.setName(SCTASK);
        sctaskStackedColumnModel.setShowInLegend("true");
        sctaskStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel ptaskStackedColumnModel = new StackedColumnModel();
        ptaskStackedColumnModel.setType(STACKED_COLUMN);
        ptaskStackedColumnModel.setName(PTASK);
        ptaskStackedColumnModel.setShowInLegend("true");
        ptaskStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel ritmStackedColumnModel = new StackedColumnModel();
        ritmStackedColumnModel.setType(STACKED_COLUMN);
        ritmStackedColumnModel.setName(RITM);
        ritmStackedColumnModel.setShowInLegend("true");
        ritmStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel prbStackedColumnModel = new StackedColumnModel();
        prbStackedColumnModel.setType(STACKED_COLUMN);
        prbStackedColumnModel.setName(PRB);
        prbStackedColumnModel.setShowInLegend("true");
        prbStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel dataCorrectionStackedColumnModel = new StackedColumnModel();
        dataCorrectionStackedColumnModel.setType(STACKED_COLUMN);
        dataCorrectionStackedColumnModel.setName("Data Correction");
        dataCorrectionStackedColumnModel.setShowInLegend("true");
        dataCorrectionStackedColumnModel.setyValueFormatString("## Inc");

        StackedColumnModel dataClarificationStackedColumnModel = new StackedColumnModel();
        dataClarificationStackedColumnModel.setType(STACKED_COLUMN);
        dataClarificationStackedColumnModel.setName("Data Clarification");
        dataClarificationStackedColumnModel.setShowInLegend("true");
        dataClarificationStackedColumnModel.setyValueFormatString("##  Inc");

        List<DataPointsModel> ctaskDataPoint = new ArrayList<>();
        List<DataPointsModel> sctaskDataPoint = new ArrayList<>();
        List<DataPointsModel> ptaskDataPoint = new ArrayList<>();
        List<DataPointsModel> ritmDataPoint = new ArrayList<>();
        List<DataPointsModel> prbDataPoint = new ArrayList<>();
        List<DataPointsModel> dataCorrectionPoint = new ArrayList<>();
        List<DataPointsModel> dataClarificationPoint = new ArrayList<>();


        int i=1;
        LocalDate tempDate = null;


        while(!fromDate.isEqual(toDate)){
            tempDate =fromDate;
            fromDate = fromDate.plusMonths(i);
            if(fromDate.isAfter(toDate)){
                fromDate = toDate;
            }


            DataPointsModel dataPointsModel = new DataPointsModel();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(tempDate, fromDate);
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals("CH & CTASK".toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            ctaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(SCTASK.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            sctaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(PTASK.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            ptaskDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(RITM.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            ritmDataPoint.add(dataPointsModel);
            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(slfReportDetails.stream()
                                                 .filter(obj -> obj.getStream().equals(PRB.toUpperCase()))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            prbDataPoint.add(dataPointsModel);

            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(fetchReportDetailsOnBasesOfDate(tempDate,fromDate).stream()
                                                 .filter(obj -> obj.getCategorization()
                                                                   .equals(DATA_CORRECTION) || obj.getCategorization()
                                                                                                  .equals(DATA_REPUSH) || obj.getCategorization()
                                                                                                                             .equals(DATA_MISSING))
                                                 .count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            dataCorrectionPoint.add(dataPointsModel);

            dataPointsModel = new DataPointsModel();
            dataPointsModel.setY(reportRepository.findAllByDateBetweenAndCategorization(tempDate,fromDate,DATA_CLARIFICATION).stream().count());
            dataPointsModel.setLabel(tempDate.getMonth()+"-"+tempDate.getYear());
            dataClarificationPoint.add(dataPointsModel);
        }


        ptaskStackedColumnModel.setDataPoints(ptaskDataPoint);
        ritmStackedColumnModel.setDataPoints(ritmDataPoint);
        dataCorrectionStackedColumnModel.setDataPoints(dataCorrectionPoint);
        dataClarificationStackedColumnModel.setDataPoints(dataClarificationPoint);


        data.add(dataCorrectionStackedColumnModel);
        data.add(dataClarificationStackedColumnModel);
        data.add(ptaskStackedColumnModel);
        data.add(ritmStackedColumnModel);
        data.add(prbStackedColumnModel);
        return data;
    }

    public Result fetchMonthlyDetails(LocalDate fromDate,LocalDate toDate){
        List<HeaderDetails> headerDetails = new ArrayList<>();
        HeaderDetails headerDetail = new HeaderDetails();
        headerDetail.setHeaderName("Types");
        headerDetail.setField(PRIORITY);
        headerDetail.setPinned(LEFT);
        headerDetails.add(headerDetail);

        LocalDate tempDate = null;
        AtomicInteger i = new AtomicInteger();
        List<Map<String, String>> rowDetails = new ArrayList<>();
        Map<String,String> ctaskData= new HashMap<>();
        Map<String,String> sctaskData= new HashMap<>();
        Map<String,String> ptaskData= new HashMap<>();
        Map<String,String> ritmData= new HashMap<>();
        Map<String,String> prbData= new HashMap<>();
        Map<String,String> clarificationData= new HashMap<>();
        Map<String,String> correctionData= new HashMap<>();

        ptaskData.put(PRIORITY,PTASK);
        ritmData.put(PRIORITY,RITM);
        correctionData.put(PRIORITY,DATA_CORRECTION);
        clarificationData.put(PRIORITY,DATA_CLARIFICATION);


        while(!fromDate.isEqual(toDate)){
            tempDate =fromDate;
            fromDate = fromDate.plusMonths(1);
            if(fromDate.isAfter(toDate)){
                fromDate = toDate;
            }
            HeaderDetails details = new HeaderDetails();
            List<ReportDetails> slfReportDetails = fetchReportDetailsOnBasesOfDate(tempDate, fromDate);

            String index = "W" + (i.incrementAndGet());
            Map<String,String> taskIncident = new HashMap<>();
            ctaskData.put(index,String.valueOf(slfReportDetails.stream().filter(rec -> rec.getStream().equals("CH & CTASK")).count()));

            sctaskData.put(index,String.valueOf(slfReportDetails.stream().filter(rec -> rec.getStream().equals(SCTASK)).count()));
            ptaskData.put(index,String.valueOf(slfReportDetails.stream().filter(rec -> rec.getStream().equals(PTASK)).count()));
            ritmData.put(index,String.valueOf(slfReportDetails.stream().filter(rec -> rec.getStream().equals(RITM)).count()));
            prbData.put(index,String.valueOf(slfReportDetails.stream().filter(rec -> rec.getStream().equals(PRB)).count()));
            correctionData.put(index,String.valueOf(slfReportDetails.stream().filter(obj -> obj.getCategorization().equals(DATA_CORRECTION) || obj.getCategorization()
                                                                         .equals(DATA_REPUSH) || obj.getCategorization()
                                                                                                    .equals(DATA_MISSING)).count()));
            clarificationData.put(index,String.valueOf(reportRepository.findAllByDateBetweenAndCategorization(tempDate,fromDate,DATA_CLARIFICATION).stream().count()));


            details.setHeaderName(tempDate.getMonth()+"-"+tempDate.getYear());
            details.setField(index);
            headerDetails.add(details);
        };
        rowDetails.add(ptaskData);
        rowDetails.add(ritmData);
        rowDetails.add(correctionData);
        rowDetails.add(clarificationData);

        Result result = new Result();
        result.setColumnDef(headerDetails);
        result.setRowData(rowDetails);
        return result;





//        List<WeeklyRequestParam> weeklyRequestParams = FridayAndThursdayDates.getWeeklyDays(year);
//        List<Map<String, String>> rowDetails = new ArrayList<>();
//        List<String> sheetNames = fetchSheetNamesWithTask();
//        for(String sheetName : sheetNames){
//            Map<String, String> consumerMap = new HashMap<>();
//            consumerMap.put(PRIORITY, sheetName);
//            AtomicInteger i = new AtomicInteger();
//            weeklyRequestParams.forEach(rec -> {
//                List<ReportDetails> slfReportDetails =
//                        fetchReportDetailsOnBasesOfDateAndConsumer(rec.getFromDate(), rec.getToDate(), sheetName);
//                consumerMap.put("W" + i.incrementAndGet(),
//                                String.valueOf(slfReportDetails.stream()
//                                                               .filter(obj -> obj.getCategorization()
//                                                                                 .equals(DATA_CORRECTION) || obj.getCategorization()
//                                                                                                                .equals(DATA_REPUSH) || obj.getCategorization()
//                                                                                                                                           .equals(DATA_MISSING))
//                                                               .count()));
//            });
//            rowDetails.add(consumerMap);
//        }


    }




    public Long calculateIncidentNo( List<ReportDetails> sqlResult , String priority) {

        Long count = 0L;

        for(ReportDetails reportDetail : sqlResult) {
            if(priority.compareTo(reportDetail.getPriority().trim()) == 0) {
                count++;
            }
        }
        return count;
    }
}
