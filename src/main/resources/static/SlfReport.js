

$(document)
		.ready(
				function() {
                   $(".dropdown-menu li a").click(function(){
                     var selText = $(this).text();
                     $(this).parents('.dropdown').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
                     $.ajax({
                     											url : "../slfReport/stacked-chart?year="+selText,
                     											type : 'Post',
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


                        $.ajax({
                                url : "../slfReport/header-details/"+selText,
                                type : 'Post',
                                success : function(result) {
                                        var data = eval(result);
                                        console.log(data.columnDef);
                                        // specify the data

                                        // let the grid know which columns and what data to use
                                        var gridOptions = {
                                        groupIncludeFooter: true,
                                        groupIncludeTotalFooter: true,
                                          animateRows: true,
                                          columnDefs: data.columnDef,
                                          rowData: data.rowData
                                        };

                                        // setup the grid after the page has finished loading
                                         var gridDiv = document.querySelector('#totalNoOfInident');
                                          new agGrid.Grid(gridDiv, gridOptions);

                                }
                            });


                                                 $.ajax({
                                                            url : "../slfReport/task-details/"+selText,
                                                            type : 'Post',
                                                            success : function(result) {
                                                                    var data = eval(result);
                                                                    console.log(data.columnDef);
                                                                    // specify the data

                                                                    // let the grid know which columns and what data to use
                                                                    var gridOptions = {
                                                                    groupIncludeFooter: true,
                                                                    groupIncludeTotalFooter: true,
                                                                      animateRows: true,
                                                                      columnDefs: data.columnDef,
                                                                      rowData: data.rowData
                                                                    };

                                                                    // setup the grid after the page has finished loading
                                                                     var gridDiv = document.querySelector('#taskIncident');
                                                                      new agGrid.Grid(gridDiv, gridOptions);

                                                            }
                                                        });
                                                                         $.ajax({
                                                                                        url : "../slfReport/openshift-details/"+selText,
                                                                                        type : 'Post',
                                                                                        success : function(result) {
                                                                                                var data = eval(result);
                                                                                                console.log(data.columnDef);
                                                                                                // specify the data

                                                                                                // let the grid know which columns and what data to use
                                                                                                var gridOptions = {
                                                                                                groupIncludeFooter: true,
                                                                                                groupIncludeTotalFooter: true,
                                                                                                  animateRows: true,
                                                                                                  columnDefs: data.columnDef,
                                                                                                  rowData: data.rowData
                                                                                                };

                                                                                                // setup the grid after the page has finished loading
                                                                                                 var gridDiv = document.querySelector('#openShiftIncident');
                                                                                                  new agGrid.Grid(gridDiv, gridOptions);

                                                                                        }
                                                                                    });

                                                                      $.ajax({
                                                                                        url : "../slfReport/landing-details/"+selText,
                                                                                        type : 'Post',
                                                                                        success : function(result) {
                                                                                                var data = eval(result);
                                                                                                console.log(data.columnDef);
                                                                                                // specify the data

                                                                                                // let the grid know which columns and what data to use
                                                                                                var gridOptions = {
                                                                                                groupIncludeFooter: true,
                                                                                                groupIncludeTotalFooter: true,
                                                                                                  animateRows: true,
                                                                                                  columnDefs: data.columnDef,
                                                                                                  rowData: data.rowData
                                                                                                };

                                                                                                // setup the grid after the page has finished loading
                                                                                                 var gridDiv = document.querySelector('#landingIncident');
                                                                                                  new agGrid.Grid(gridDiv, gridOptions);

                                                                                        }
                                                                                    });

                        
                        $.ajax({
                            url : "../slfReport/batch/"+selText,
                            type : 'Post',
                            success : function(result) {
                                    var data = eval(result);
                                    console.log(data.columnDef);
                                    // specify the data

                                    // let the grid know which columns and what data to use
                                    var gridOptions = {
                                    groupIncludeFooter: true,
                                    groupIncludeTotalFooter: true,
                                      animateRows: true,
                                      columnDefs: data.columnDef,
                                      rowData: data.rowData
                                    };

                                    // setup the grid after the page has finished loading
                                     var gridDiv = document.querySelector('#batchesIncident');
                                      new agGrid.Grid(gridDiv, gridOptions);

                            }
                        });
                        
                        $.ajax({
                            url : "../slfReport/idrs/"+selText,
                            type : 'Post',
                            success : function(result) {
                                    var data = eval(result);
                                    console.log(data.columnDef);
                                    // specify the data

                                    // let the grid know which columns and what data to use
                                    var gridOptions = {
                                    groupIncludeFooter: true,
                                    groupIncludeTotalFooter: true,
                                      animateRows: true,
                                      columnDefs: data.columnDef,
                                      rowData: data.rowData
                                    };

                                    // setup the grid after the page has finished loading
                                     var gridDiv = document.querySelector('#idrsIncident');
                                      new agGrid.Grid(gridDiv, gridOptions);

                            }
                        });

                   });
       });

//					$("#button")
//							.click(
//									function() {
//										var dateFormat = "yyyy-mm-dd";
//										var fromDt = new Date($('#from')
//												.datepicker("getDate"));
//										var toDt = new Date($('#to')
//												.datepicker("getDate"));
//
//										var fromDate = convertDate(fromDt);
//										var toDate = convertDate(toDt);
//
//										$
//												.ajax({
//													url : "../slfReport?year="+year,
//													type : 'GET',
//													success : function(result) {
//													for(let key in result){
//
//														$("#totalNoOfInident")
//																.html(
//																		createTable(result.totalNoOfIncident));
//														$("#taskIncident")
//																.html(
//																		createTable(result.taskIncident));
//														$("#openShiftIncident")
//																.html(
//																		createTable(result.openShiftIncident));
//														$("#landingIncident")
//																.html(
//																		createTable(result.landingIncident));
//														$("#batchesIncident")
//																.html(
//																		createTable(result.batchesIncident));
//														$("#idsIncident")
//																.html(
//																		createTable(result.idsIncident));
//														$(
//																"#dataClarificationIncident")
//																.html(
//																		createTable(result.dataClarificationIncident));
//														$(
//																"#dataCorrectionIncident")
//																.html(
//																		createTable(result.dataCorrectionIncident));
//
//														$(
//																"#totalNoOfInidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.totalNoOfIncident));
//														$("#taskIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.taskIncident));
//														$(
//																"#openShiftIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.openShiftIncident));
//														$(
//																"#landingIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.landingIncident));
//														$(
//																"#batchesIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.batchesIncident));
//														$("#idsIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.idsIncident));
//														$(
//																"#dataClarificationIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.dataClarificationIncident));
//														$(
//																"#dataCorrectionIncidentTotal")
//																.text(
//																		"Total no. "
//																				+ getTotal(result.dataCorrectionIncident));
//													}
//													}
//												});
//
//
//					function createTable(obj) {
//						var text = "<table class='table'><tr><th>Priority</th><th></th></tr>";
//
//						for ( var key in obj) {
//							if (obj[key] != "0") {
//								text += "<tr style='background-color: rgba(72, 113, 248, 0.068);'><td>"
//										+ key
//										+ "</td><td>"
//										+ obj[key]
//										+ "</td></tr>"
//							}
//						}
//						text += "</table>";
//						return text;
//					}
//					function getTotal(obj) {
//						var total = 0;
//						for ( var key in obj) {
//							if (obj[key] != "0") {
//								total += Number(obj[key]);
//							}
//						}
//						return total;
//					}
//					function convertDate(date) {
//						var yyyy = date.getFullYear().toString();
//						var mm = (date.getMonth() + 1).toString();
//						var dd = date.getDate().toString();
//
//						var mmChars = mm.split('');
//						var ddChars = dd.split('');
//
//						return yyyy + '-'
//								+ (mmChars[1] ? mm : "0" + mmChars[0]) + '-'
//								+ (ddChars[1] ? dd : "0" + ddChars[0]);
//					}
//
//				});