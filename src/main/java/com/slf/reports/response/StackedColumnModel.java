package com.slf.reports.response;

import java.util.List;

public class StackedColumnModel {

    private String type;
    private String name;
    private String showInLegend;
    private String yValueFormatString;
    private List<?> dataPoints;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowInLegend() {
        return showInLegend;
    }

    public void setShowInLegend(String showInLegend) {
        this.showInLegend = showInLegend;
    }

    public String getyValueFormatString() {
        return yValueFormatString;
    }

    public void setyValueFormatString(String yValueFormatString) {
        this.yValueFormatString = yValueFormatString;
    }

    public List<?> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<?> dataPoints) {
        this.dataPoints = dataPoints;
    }

}
