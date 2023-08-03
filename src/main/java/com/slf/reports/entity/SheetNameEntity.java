package com.slf.reports.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@Builder
public class SheetNameEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long sheetNo;
    private String stream;

    public SheetNameEntity() {
    }

    public SheetNameEntity(long sheetNo, String stream) {
        this.sheetNo = sheetNo;
        this.stream = stream;
    }

    public long getSheetNo() {
        return sheetNo;
    }

    public void setSheetNo(long sheetNo) {
        this.sheetNo = sheetNo;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "SheetNameEntity{" + "sheetNo=" + sheetNo + ", stream='" + stream + '\'' + '}';
    }
}
