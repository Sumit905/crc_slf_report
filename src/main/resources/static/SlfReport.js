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

    		$('.header > div > a').click(function(event){
    		event.preventDefault();//stop browser to take action for clicked anchor

    		//get displaying tab content jQuery selector
    		var active_tab_selector = $('.header > div > a.active').attr('href');

    		//find actived navigation and remove 'active' css
    		var actived_nav = $('.header > div > a.active');
    		actived_nav.removeClass('active');

    		//add 'active' css into clicked navigation
    		$(this).addClass('active');


    		//hide displaying tab content
    		$(active_tab_selector).removeClass('active');
    		$(active_tab_selector).addClass('hide');

    		//show target tab content
    		var target_tab_selector = $(this).attr('href');
    		$(target_tab_selector).removeClass('hide');
    		$(target_tab_selector).addClass('active');
    	     });




 var calcTotalCols = [];
  let monthlyDivId = document.querySelector('#dataMonthlyReport');
                                let monthlyIncidentAgGrid = new agGrid.Grid(monthlyDivId, {
                                                                         animateRows: true,
                                                                         columnDefs: [],
                                                                         rowData: []
                                                                   });

				$("#from").datepicker({
                        dateFormat: 'yy-mm-dd',
                        changeMonth: true,
                        changeYear: true,
                        changeDate:true,
                        showButtonPanel: true,
                        beforeShow: function(input, inst) {
                            if (!$(this).val()) {
                                //$(this).datepicker('setDate', new Date(inst.selectedYear, inst.selectedMonth, 1)).trigger('change');
                            }
                        },
                        onClose: function(dateText, inst) {
                            $("#to").datepicker("option", {minDate: new Date(inst.selectedYear, inst.selectedMonth, 1)})
                        }
                    });
                    $('#from').datepicker('setDate', new Date());
                    $('#to').datepicker({
                        dateFormat: 'yy-mm-dd',
                        changeMonth: true,
                        changeYear: true,
                        changeDate:true,
                        showButtonPanel: true,
                        onClose: function(dateText, inst) {
                           // $(this).datepicker('setDate', new Date(inst.selectedYear, inst.selectedMonth, 1)).trigger('change');
                        }
                    });

                    $("#btnShow").click(function() {
                        if ($("#from").val().length == 0 || $("#to").val().length == 0) {
                            alert('All fields are required');
                        } else {
                            var startDay = $("#from").val();
                            var endDay = $("#to").val();

                            $.ajax({
                                url : "../slfReport/monthly?fromDate="+startDay+"&toDate="+endDay,
                                type : 'Post',
                                success : function(result) {
                                       var data = eval(result);
                                      var chart = new CanvasJS.Chart("monthlyReportChart", {
                                              animationEnabled : true,
                                              indexLabel: "Total: {y}",
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


                             $.ajax({
                                   url : '../slfReport/data/monthly-details?fromDate='+$("#from").val()+"&toDate="+$("#to").val(),
                                   type : 'Post',
                                   success : function(result) {
                                          var data = eval(result);
                                          data.columnDef.forEach(params => params.cellRenderer= LinkRenderer);
                                         monthlyIncidentAgGrid.gridOptions.api.setColumnDefs(data.columnDef);
                                         monthlyIncidentAgGrid.gridOptions.api.setRowData(data.rowData);
                                          calcTotalCols=data.columnDef;
                                          totalRow(monthlyIncidentAgGrid.gridOptions.api,data.rowData);
                                   }
                               });

                        }
                    });


				const myModalEl = document.getElementById('exampleModal');
				const mySubmitFormFileEl = document.getElementById('submitFormFile');
				const myGridElement = document.querySelector('#modalTable');
				const myAlert = document.querySelector('#alert');
				const mySpinnerId = document.querySelector('#spinnerId');
				const modalAgGridOptions = {};
                let modalAgGridTable = new agGrid.Grid(myGridElement, modalAgGridOptions);
                var tabIndex =[{id:'v-pills-inc-tab',tab:'total-incident'},
                    {id:'v-pills-task-tab',tab:'task-details'},
                    {id:'v-pills-open-shift-tab',tab:'openshift-details'},
                    {id:'v-pills-batches-tab',tab:'batch-details'},
                    {id:'v-pills-idrs-tab',tab:'idrs-details'},
                    {id:'v-pills-data-clarification-tab',tab:'data-clarification-details'},
                    {id:'v-pills-data-correction-tab',tab:'data-correction-details'}]

                    mySubmitFormFileEl.addEventListener('click', event => {
                        var formData = new FormData();
                        formData.append('file', $('#formFile')[0].files[0]);
                        $.ajax({
                                url : "../excel",
                                type : 'Post',
                                processData: false,
                                contentType: false,
                                cache: false,
                                data: formData,
                                success : function(result) {
                                myAlert.innerHTML('<div class="alert alert-warning alert-dismissible fade show" role="alert">'+
                                       result+
                                        '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>'+
                                        '</button></div>');
                                }
                            });

                    });
                    myModalEl.addEventListener('show.bs.modal', event => {
                     // do something...
                     var selText = $("#dropdownMenuButton1 > label.btn.active").text().trim();
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
                          var selText = $(this).parents('.container-fluid').find("#dropdownMenuButton1 > label.btn.active").text().trim();
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
                   $("label.btn").click(function(){
                     $("#dropdownMenuButton1 > label.btn").removeClass('active');
                     $(this).addClass('active')
                     var selText = $(this).text();
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
                                   url : rec.gridUrl=='../slfReport/data/monthly-details'? rec.gridUrl+"?fromDate="+$("#from").val()+"&toDate="+$("#to").val() :rec.gridUrl+selText,
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

                   $("#exportButton").click(function(e){
                        for(let rec of gridOptions){
                             e.preventDefault();
                             exportToPDF(rec.gridOpt,"Test");
                        }
                   });

       });



//Create PDf from HTML...
function CreatePDFfromHTML() {
    var HTML_Width = $("#v-pills-tabContent").width();
        var HTML_Height = $("#v-pills-tabContent").height();
        var top_left_margin = 15;
        var PDF_Width = HTML_Width + (top_left_margin * 2);
        var PDF_Height = (PDF_Width * 1.5) + (top_left_margin * 2);
        var canvas_image_width = HTML_Width;
        var canvas_image_height = HTML_Height;

        var totalPDFPages = Math.ceil(HTML_Height / PDF_Height) - 1;

        html2canvas($("#v-pills-tabContent")[0]).then(function (canvas) {
            var imgData = canvas.toDataURL("image/jpeg", 1.0);
            var pdf = new jsPDF('p', 'pt', [PDF_Width, PDF_Height]);
            pdf.addImage(imgData, 'JPG', top_left_margin, top_left_margin, canvas_image_width, canvas_image_height);
            for (var i = 1; i <= totalPDFPages; i++) {
                pdf.addPage(PDF_Width, PDF_Height);
                pdf.addImage(imgData, 'JPG', top_left_margin, -(PDF_Height*i)+(top_left_margin*4),canvas_image_width,canvas_image_height);
            }
            pdf.save("Your_PDF_Name.pdf");
        });
}