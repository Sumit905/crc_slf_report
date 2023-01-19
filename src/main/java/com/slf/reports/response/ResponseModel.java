package com.slf.reports.response;

import java.util.Map;

public class ResponseModel {

	private Map<String, Long> totalNoOfIncident;

	private Map<String, Long> taskIncident;

	private Map<String, Long> openShiftIncident;

	private Map<String, Long> landingIncident;

	private Map<String, Long> batchesIncident;

	private Map<String, Long> idsIncident;

	private Map<String, Long> dataClarificationIncident;

	private Map<String, Long> dataCorrectionIncident;

	public ResponseModel() {
		super();
	}

	public ResponseModel(Map<String, Long> totalNoOfIncident, Map<String, Long> taskIncident,
			Map<String, Long> openShiftIncident, Map<String, Long> landingIncident, Map<String, Long> batchesIncident,
			Map<String, Long> idsIncident, Map<String, Long> dataClarificationIncident,
			Map<String, Long> dataCorrectionIncident) {
		super();
		this.totalNoOfIncident = totalNoOfIncident;
		this.taskIncident = taskIncident;
		this.openShiftIncident = openShiftIncident;
		this.landingIncident = landingIncident;
		this.batchesIncident = batchesIncident;
		this.idsIncident = idsIncident;
		this.dataClarificationIncident = dataClarificationIncident;
		this.dataCorrectionIncident = dataCorrectionIncident;
	}

	public Map<String, Long> getTotalNoOfIncident() {
		return totalNoOfIncident;
	}

	public void setTotalNoOfIncident(Map<String, Long> totalNoOfIncident) {
		this.totalNoOfIncident = totalNoOfIncident;
	}

	public Map<String, Long> getTaskIncident() {
		return taskIncident;
	}

	public void setTaskIncident(Map<String, Long> taskIncident) {
		this.taskIncident = taskIncident;
	}

	public Map<String, Long> getOpenShiftIncident() {
		return openShiftIncident;
	}

	public void setOpenShiftIncident(Map<String, Long> openShiftIncident) {
		this.openShiftIncident = openShiftIncident;
	}

	public Map<String, Long> getLandingIncident() {
		return landingIncident;
	}

	public void setLandingIncident(Map<String, Long> landingIncident) {
		this.landingIncident = landingIncident;
	}

	public Map<String, Long> getBatchesIncident() {
		return batchesIncident;
	}

	public void setBatchesIncident(Map<String, Long> batchesIncident) {
		this.batchesIncident = batchesIncident;
	}

	public Map<String, Long> getIdsIncident() {
		return idsIncident;
	}

	public void setIdsIncident(Map<String, Long> idsIncident) {
		this.idsIncident = idsIncident;
	}

	public Map<String, Long> getDataClarificationIncident() {
		return dataClarificationIncident;
	}

	public void setDataClarificationIncident(Map<String, Long> dataClarificationIncident) {
		this.dataClarificationIncident = dataClarificationIncident;
	}

	public Map<String, Long> getDataCorrectionIncident() {
		return dataCorrectionIncident;
	}

	public void setDataCorrectionIncident(Map<String, Long> dataCorrectionIncident) {
		this.dataCorrectionIncident = dataCorrectionIncident;
	}

}
