package com.slf.reports.response;

public class HeaderDetails {

    private String headerName;
    private String field;

    private String pinned;

    private String tooltipField;

    private Integer width;

    private boolean suppressSizeToFit;

    private Integer maxWidth;

    private Integer flex;


    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPinned() {
        return pinned;
    }

    public void setPinned(String pinned) {
        this.pinned = pinned;
    }

    public String getTooltipField() {
        return tooltipField;
    }

    public void setTooltipField(String tooltipField) {
        this.tooltipField = tooltipField;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public boolean isSuppressSizeToFit() {
        return suppressSizeToFit;
    }

    public void setSuppressSizeToFit(boolean suppressSizeToFit) {
        this.suppressSizeToFit = suppressSizeToFit;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public Integer getFlex() {
        return flex;
    }

    public void setFlex(Integer flex) {
        this.flex = flex;
    }
}
