package com.slf.reports.request;

import java.time.LocalDate;

public class WeeklyRequestParam {
    private LocalDate fromDate;
    private LocalDate toDate;

    private String weeklyId;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getWeeklyId() {
        return weeklyId;
    }

    public void setWeeklyId(String weeklyId) {
        this.weeklyId = weeklyId;
    }
}
