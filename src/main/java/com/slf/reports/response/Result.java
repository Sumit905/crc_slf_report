package com.slf.reports.response;

import java.util.List;
import java.util.Map;

public class Result {

    private List<HeaderDetails> columnDef;
    private List<Map<String,String>> rowData;

    public List<HeaderDetails> getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(List<HeaderDetails> columnDef) {
        this.columnDef = columnDef;
    }

    public List<Map<String, String>> getRowData() {
        return rowData;
    }

    public void setRowData(List<Map<String, String>> rowData) {
        this.rowData = rowData;
    }
}
