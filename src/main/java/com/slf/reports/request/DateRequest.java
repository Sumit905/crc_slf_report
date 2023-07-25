package com.slf.reports.request;

import java.util.List;

public class DateRequest {
    private List<WeeklyRequestParam> weeklyRequestParamList;

    public List<WeeklyRequestParam> getWeeklyRequestParamList() {
        return weeklyRequestParamList;
    }

    public void setWeeklyRequestParamList(List<WeeklyRequestParam> weeklyRequestParamList) {
        this.weeklyRequestParamList = weeklyRequestParamList;
    }
}
