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


				const myModalEl = document.getElementById('exampleModal')
				const myGridElement = document.querySelector('#modalTable');
				const modalAgGridOptions = {};
                let modalAgGridTable = new agGrid.Grid(myGridElement, modalAgGridOptions);
                var tabIndex =[{id:'v-pills-inc-tab',tab:'total-incident'},
                    {id:'v-pills-task-tab',tab:'task-details'},
                    {id:'v-pills-open-shift-tab',tab:'openshift-details'},
                    {id:'v-pills-landing-tab',tab:'landing-details'},
                    {id:'v-pills-batches-tab',tab:'batch-details'},
                    {id:'v-pills-idrs-tab',tab:'idrs-details'},
                    {id:'v-pills-data-clarification-tab',tab:'data-clarification-details'},
                    {id:'v-pills-data-correction-tab',tab:'data-correction-details'}]
                    myModalEl.addEventListener('show.bs.modal', event => {
                     // do something...
                     var selText = $("#dropdownMenuButton1").text().trim();
                     var columnId = $(event.relatedTarget).data('column-id');
                     var priority = $(event.relatedTarget).data('priority');
                     var columnValue = $(event.relatedTarget).data('column-value');
                     var tab = tabIndex.find(rec => rec.id== $("#v-pills-tab .nav-link.active")[0].id).tab;
                    var modalTitle = myModalEl.querySelector('.modal-title')
                    var modalBodyInput = myModalEl.querySelector('.modal-body input')
                     modalTitle.textContent = priority+" ( " + columnValue +" )";

                     $.ajax({
                            url : "../slfReport/"+tab+"/"+priority+"/"+columnId+"/"+selText,
                            type : 'Post',
                            success : function(result) {
                                   var data = eval(result);
                                   modalAgGridTable.gridOptions.api.setColumnDefs(data.columnDef);
                                   modalAgGridTable.gridOptions.api.setRowData(data.rowData);
                                   modalAgGridTable.gridOptions.api.setAutoSizeStrategy({
                                                                                      type: 'fitGridWidth',
                                                                                      defaultMinWidth: 100,
                                                                                      columnLimits: [
                                                                                        {
                                                                                          colId: 'notes',
                                                                                          minWidth: 200,
                                                                                        },
                                                                                      ],
                                                                                    });

                            }
                        });
                   });
                    const tablesList = [
                    {
                        tableId: 'totalNoOfInident',
                        tableUrl: '../slfReport/total-incident/'
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
                        tableUrl: '../slfReport/batch-details/'
                    },{
                        tableId: 'idrsIncident',
                        tableUrl: '../slfReport/idrs-details/'
                    },{
                        tableId: 'dataClarificationIncident',
                        tableUrl: '../slfReport/data/clarification-details/'
                    },{
                        tableId: 'dataCorrectionIncident',
                        tableUrl: '../slfReport/data/correction-details/'
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
                          var selText = $(this).parents('.container-fluid').find('#dropdownMenuButton1').text().trim();
                          switch(this.id){
                                case "v-pills-inc-tab":{
                                    $.ajax({
                                            url : "../slfReport/total-incident/chart/"+selText,
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
                                        url : "../slfReport/task-details/chart/"+selText,
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
                                    url : "../slfReport/openshift-details/chart/"+selText,
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
                                        url : "../slfReport/landing-details/chart/"+selText,
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
                                    url : "../slfReport/batch-details/chart/"+selText,
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
                                    url : "../slfReport/idrs-details/chart/"+selText,
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
                         url : "../slfReport/total-incident/chart/"+selText,
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
                                          data.columnDef.forEach(params => params.cellRenderer= LinkRenderer);
                                          rec.gridOpt.gridOptions.api.setColumnDefs(data.columnDef);
                                          rec.gridOpt.gridOptions.api.setRowData(data.rowData);
                                          calcTotalCols=data.columnDef;
                                          totalRow(rec.gridOpt.gridOptions.api,data.rowData);
                                   }
                               });
                        }


                   });
       });