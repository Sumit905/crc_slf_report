package com.slf.reports.utils;

import com.slf.reports.request.WeeklyRequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FridayAndThursdayDates {

    public static List<WeeklyRequestParam> getWeeklyDays(int year){
        List<WeeklyRequestParam> weeklyDays= new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate friday = LocalDate.of(year, 1, 1)
                                    .with(TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY));
        AtomicInteger i = new AtomicInteger();
        while (friday.getYear() == year && friday.compareTo(currentDate)<=0) {
            WeeklyRequestParam weeklyRequestParam = new WeeklyRequestParam();
            LocalDate nextThursday = friday.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
            weeklyRequestParam.setFromDate(friday);
            weeklyRequestParam.setToDate(nextThursday);
            weeklyRequestParam.setWeeklyId("W"+i.incrementAndGet());
            weeklyDays.add(weeklyRequestParam);
            friday = friday.plusWeeks(1);
        }
        return weeklyDays;
    }


}
