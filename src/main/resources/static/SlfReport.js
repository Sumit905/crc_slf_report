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


                    $('#v-pills-tab a').on('click', function (e) {
                                      e.preventDefault()
                          var selText = $(this).parents('.container').find('#dropdownMenuButton1').text().trim();
                          switch(this.id){
                                case "v-pills-inc-tab":{
                                    $.ajax({
                                            url : "../slfReport/chart/stacked/"+selText,
                                            type : 'Post',
                                            contentType : "application/json; charset=utf-8",
                                            success : function(result) {
                                                var chart = new CanvasJS.Chart("totalNoOfInidentChart", {
                                                    animationEnabled : true,
                                                    title : {
                                                        text : "Total Number of Incidents"
                                                    },
                                                    axisY : {
                                                        title : "Number Of Incidents"
                                                    },
                                                    axisX: {
                                                        interval: 1
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
                                 break;
                                }
                               case "v-pills-task-tab":{
                                    $.ajax({
                                        url : "../slfReport/chart/task?year="+selText,
                                        type : 'Post',
                                        contentType : "application/json; charset=utf-8",
                                        success : function(result) {
                                            var chart = new CanvasJS.Chart("taskIncidentChart", {
                                                animationEnabled : true,
                                                title : {
                                                    text : "Total Number of Task Incidents"
                                                },
                                                axisY : {
                                                    title : "Number Of Incidents"
                                                },
                                                axisX: {
                                                    interval: 1
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
                                    break;
                               }
                               case "v-pills-open-shift-tab":
                               {
                                $.ajax({
                                    url : "../slfReport/chart/open-shift?year="+selText,
                                    type : 'Post',
                                    contentType : "application/json; charset=utf-8",
                                    success : function(result) {
                                        var chart = new CanvasJS.Chart("openShiftIncidentChart", {
                                            animationEnabled : true,
                                            title : {
                                                text : "Open Shift Incidents"
                                            },
                                            axisY : {
                                                title : "Number Of Incidents"
                                            },
                                            axisX: {
                                                interval: 1
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
                                break;
                               }
                               case "v-pills-landing-tab":
                               {
                                    $.ajax({
                                        url : "../slfReport/chart/landing?year="+selText,
                                        type : 'Post',
                                        contentType : "application/json; charset=utf-8",
                                        success : function(result) {
                                            var chart = new CanvasJS.Chart("landingIncidentChart", {
                                                animationEnabled : true,
                                                title : {
                                                    text : "Landing Incidents"  
                                                },
                                                axisY : {
                                                    title : "Number Of Incidents"
                                                },
                                                axisX: {
                                                    interval: 1
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
                                   break;
                               }
                               case "v-pills-batches-tab":
                              {
                                $.ajax({
                                    url : "../slfReport/chart/batches?year="+selText,
                                    type : 'Post',
                                    contentType : "application/json; charset=utf-8",
                                    success : function(result) {
                                        var chart = new CanvasJS.Chart("batchesIncidentChart", {
                                            animationEnabled : true,
                                            title : {
                                                text : "Batches Incidents"
                                            },
                                            axisY : {
                                                title : "Number Of Incidents"
                                            },
                                            axisX: {
                                                interval: 1
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
                                  break;
                              }
                              case "v-pills-idrs-tab":
                                {
                                $.ajax({
                                    url : "../slfReport/chart/idrs?year="+selText,
                                    type : 'Post',
                                    contentType : "application/json; charset=utf-8",
                                    success : function(result) {
                                        var chart = new CanvasJS.Chart("idrsIncidentChart", {
                                            animationEnabled : true,
                                            title : {
                                                text : "IDRS Incidents"
                                            },
                                            axisY : {
                                                title : "Number Of Incidents"
                                            },
                                            axisX: {
                                                interval: 1
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
                                    break;
                                }

                          }
                          $(this).tab('show');
                    });
                   $(".dropdown-menu li a").click(function(){
                     var selText = $(this).text();
                     $(this).parents('.dropdown').find('#dropdownMenuButton1').html(selText+' <span class="caret"></span>');
                      $.ajax({
                         url : "../slfReport/stacked-chart?year="+selText,
                         type : 'Post',
                         contentType : "application/json; charset=utf-8",
                         success : function(result) {
                             var chart = new CanvasJS.Chart("totalNoOfInidentChart", {
                                 animationEnabled : true,
                                 title : {
                                     text : "Total Number of Incidents"
                                 },
                                 axisY : {
                                     title : "Number Of Incidents"
                                 },
                                 axisX: {
                                     interval: 1
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