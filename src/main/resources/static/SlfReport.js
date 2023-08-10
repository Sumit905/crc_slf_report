class Observable {
    constructor(functionThatTakesObserver){
      this._functionThatTakesObserver = functionThatTakesObserver;
    }

    subscribe(observer) {
      return this._functionThatTakesObserver(observer)
    }
}


$(document)
		.ready(
				function() {

                    const tablesList = [
                    {
                        tableId: 'totalNoOfInident',
                        tableUrl: '../slfReport/header-details/'
                    },{
                        tableId: 'taskIncident',
                        tableUrl: '../slfReport/task-details/'
                    },{
                        tableId: 'openShiftIncident',
                        tableUrl: '../slfReport/openshift-details/'
                    },{
                        tableId: 'landingIncident',
                        tableUrl: '../slfReport/landing-details/'
                    },{
                        tableId: 'batchesIncident',
                        tableUrl: '../slfReport/batch/'
                    },{
                        tableId: 'idrsIncident',
                        tableUrl: '../slfReport/idrs/'
                    },{
                        tableId: 'dataClarificationIncident',
                        tableUrl: '../slfReport/data/clarification/'
                    },{
                        tableId: 'dataCorrectionIncident',
                        tableUrl: '../slfReport/data/correction/'
                    }]
                      const gridOptions = [];
                      for (let i of tablesList) {
                           let divId = document.querySelector('#'+i.tableId);
                           let incidentAgGrid = new agGrid.Grid(divId, {
                                                                    animateRows: true,
                                                                    columnDefs: [],
                                                                    rowData: []
                                                              });
                          gridOptions.push({
                            gridOpt:incidentAgGrid,
                            gridUrl:i.tableUrl
                          });
                      }
                       var calcTotalCols = [];

                        const totalRow = function(api,rowData) {
                            let result = [{}];

                            // initialize all total columns
                            calcTotalCols.forEach(function (params){
                                result[0][params.field] = 0
                            });
                           // calculate all total columns
                            calcTotalCols.forEach(function (params){
                               rowData.forEach(function (line) {
                                    if(isNaN(line[params.field])){
                                        result[0][params.field] = "Total";
                                    } else {
                                        result[0][params.field] += parseInt(line[params.field]);
                                    }

                                });
                            });
                            api.setPinnedBottomRowData(result);
                        }



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
                        for(let rec of gridOptions){
                             $.ajax({
                                   url : rec.gridUrl+selText,
                                   type : 'Post',
                                   success : function(result) {
                                          var data = eval(result);
                                          rec.gridOpt.gridOptions.api.setColumnDefs(data.columnDef);
                                          rec.gridOpt.gridOptions.api.setRowData(data.rowData);
                                          calcTotalCols=data.columnDef;
                                          totalRow(rec.gridOpt.gridOptions.api,data.rowData);
                                   }
                               });
                        }


                   });
       });