class LinkRenderer {
  eGui;

  // init method gets the details of the cell to be renderer
  init(params) {
    this.eGui = document.createElement('span');
    this.eGui.innerHTML = parseInt(params.value)>0 && params.data['priority'] !="Total" ? "<a href='#' data-bs-toggle='modal'  data-column-value = '"+params.column.colDef.headerName+"' data-column-id='"+params.column.colId+"' data-priority='"+params.data['priority']+"' data-bs-target='#exampleModal'>"+params.value+"</a>" : params.value;
    }

  getGui() {
    return this.eGui;
  }

  refresh(params) {
    return false;
  }
}