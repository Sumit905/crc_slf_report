$(function() {
	var dateFormat = "yyyy-mm-dd", from = $("#from").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		numberOfMonths : 1
	}).on("change", function() {
		to.datepicker("option", "minDate", getDate(this));
	}), to = $("#to").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		numberOfMonths : 1
	}).on("change", function() {
		from.datepicker("option", "maxDate", getDate(this));
	});

	function getDate(element) {
		var date;
		try {
			date = $('#from').datepicker.parseDate(dateFormat, element.value);
		} catch (error) {
			date = null;
		}

		return date;
	}
});

$(document)
		.ready(
				function() {
					$("button")
							.click(
									function() {
										var dateFormat = "yyyy-mm-dd";
										var fromDt = new Date($('#from')
												.datepicker("getDate"));
										var toDt = new Date($('#to')
												.datepicker("getDate"));

										var fromDate = convertDate(fromDt);
										var toDate = convertDate(toDt);

										$
												.ajax({
													url : "../slfReport?fromDate="
															+ fromDate
															+ "&toDate="
															+ toDate,
													type : 'GET',
													success : function(result) {
														$("#totalNoOfInident")
																.html(
																		createTable(result.totalNoOfIncident));
														$("#taskIncident")
																.html(
																		createTable(result.taskIncident));
														$("#openShiftIncident")
																.html(
																		createTable(result.openShiftIncident));
														$("#landingIncident")
																.html(
																		createTable(result.landingIncident));
														$("#batchesIncident")
																.html(
																		createTable(result.batchesIncident));
														$("#idsIncident")
																.html(
																		createTable(result.idsIncident));
														$(
																"#dataClarificationIncident")
																.html(
																		createTable(result.dataClarificationIncident));
														$(
																"#dataCorrectionIncident")
																.html(
																		createTable(result.dataCorrectionIncident));

														$(
																"#totalNoOfInidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.totalNoOfIncident));
														$("#taskIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.taskIncident));
														$(
																"#openShiftIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.openShiftIncident));
														$(
																"#landingIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.landingIncident));
														$(
																"#batchesIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.batchesIncident));
														$("#idsIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.idsIncident));
														$(
																"#dataClarificationIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.dataClarificationIncident));
														$(
																"#dataCorrectionIncidentTotal")
																.text(
																		"Total no. "
																				+ getTotal(result.dataCorrectionIncident));
													}
												});
										$.ajax({
											url : "../slfReport/stacked-chart",
											type : 'Post',
											data : JSON.stringify({
												"weeklyRequestParamList" : [ {
													"fromDate" : fromDate,
													"toDate" : toDate
												}]
											}),
											contentType : "application/json; charset=utf-8",
											success : function(result) {
												var chart = new CanvasJS.Chart("chartContainer", {
													animationEnabled : true,
													title : {
														text : "Total Number of Incidents"
													},
													axisY : {
														title : "Number Of Incidents"
													},
													toolTip : {
														shared : true,
														reversed : true
													},
													data : eval(result)
												});

												chart.render();
											}
										});

									});

					function createTable(obj) {
						var text = "<table class='table'><tr><th>Priority</th><th>Value</th></tr>";

						for ( var key in obj) {
							if (obj[key] != "0") {
								text += "<tr style='background-color: rgba(72, 113, 248, 0.068);'><td>"
										+ key
										+ "</td><td>"
										+ obj[key]
										+ "</td></tr>"
							}
						}
						text += "</table>";
						return text;
					}
					function getTotal(obj) {
						var total = 0;
						for ( var key in obj) {
							if (obj[key] != "0") {
								total += Number(obj[key]);
							}
						}
						return total;
					}
					function convertDate(date) {
						var yyyy = date.getFullYear().toString();
						var mm = (date.getMonth() + 1).toString();
						var dd = date.getDate().toString();

						var mmChars = mm.split('');
						var ddChars = dd.split('');

						return yyyy + '-'
								+ (mmChars[1] ? mm : "0" + mmChars[0]) + '-'
								+ (ddChars[1] ? dd : "0" + ddChars[0]);
					}

				});