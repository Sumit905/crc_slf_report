package com.slf.reports.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.sun.istack.NotNull;

import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class TableRecordUpdate {
	
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long dataId;
	private String crccemIip;
	private String crccemTable;
	private String crcTable;
	private String query;
	private String updateQuery;
	
	
	
	public long getDataId() {
		return dataId;
	}
	public void setDataId(long dataId) {
		this.dataId = dataId;
	}
	public String getCrccemIip() {
		return crccemIip;
	}
	public void setCrccemIip(String crccemIip) {
		this.crccemIip = crccemIip;
	}
	public String getCrccemTable() {
		return crccemTable;
	}
	public void setCrccemTable(String crccemTable) {
		this.crccemTable = crccemTable;
	}
	public String getCrcTable() {
		return crcTable;
	}
	public void setCrcTable(String crcTable) {
		this.crcTable = crcTable;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getUpdateQuery() {
		return updateQuery;
	}
	public void setUpdateQuery(String updateQuery) {
		this.updateQuery = updateQuery;
	}
	
	
	
}
