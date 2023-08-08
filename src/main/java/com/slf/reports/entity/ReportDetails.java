package com.slf.reports.entity;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.LocalDate;

@Entity
@Data
@Builder
public class ReportDetails {
	
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long reportId;
	private String stream;
	private LocalDate date;
	@NotNull
	private String incidentNo;
	private String priority;
	@Lob
	private String categorization;
	public ReportDetails() {
	}
	public ReportDetails(long reportId, String stream, LocalDate date, String incidentNo, String priority,
			String categorization) {
		this.reportId = reportId;
		this.stream = stream;
		this.date = date;
		this.incidentNo = incidentNo;
		this.priority = priority;
		this.categorization = categorization;
	}
	public long getReportId() {
		return reportId;
	}
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		this.stream = stream;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public String getIncidentNo() {
		return incidentNo;
	}
	public void setIncidentNo(String incidentNo) {
		this.incidentNo = incidentNo;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getCategorization() {
		return categorization;
	}
	public void setCategorization(String categorization) {
		this.categorization = categorization;
	}
	
	@Override
	public String toString() {
		return "ReportDetails [reportId=" + reportId + ", stream=" + stream + ", date=" + date + ", incidentNo="
				+ incidentNo + ", priority=" + priority + ", categorization=" + categorization + "]";
	}

}
