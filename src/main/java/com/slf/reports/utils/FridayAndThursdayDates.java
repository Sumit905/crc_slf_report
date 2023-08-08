package com.slf.reports.utils;

import com.slf.reports.request.WeeklyRequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class FridayAndThursdayDates {

    public static List<WeeklyRequestParam> getWeeklyDays(int year){
        List<WeeklyRequestParam> weeklyDays= new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate friday = LocalDate.of(year, 1, 1)
                                    .with(TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY));
        while (friday.getYear() == year && friday.compareTo(currentDate)<=0) {
            WeeklyRequestParam weeklyRequestParam = new WeeklyRequestParam();
            //System.out.println("Friday in " + friday.getYear() + ": " + friday);
            // Find the year and date of the coming week's Thursday
            LocalDate nextThursday = friday.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
           // System.out.println("Thursday in " + friday.getYear() + ": " + nextThursday);
            weeklyRequestParam.setFromDate(friday);
            weeklyRequestParam.setToDate(nextThursday);
            weeklyDays.add(weeklyRequestParam);
            friday = friday.plusWeeks(1);
        }
        return weeklyDays;
    }


}
