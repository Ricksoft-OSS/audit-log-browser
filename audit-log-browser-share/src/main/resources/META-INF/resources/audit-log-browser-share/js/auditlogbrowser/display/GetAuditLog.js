/*
 * Copyright 2018 Ricksoft Co., Ltd.
 * All rights reserved.
 */
var AuditLogBrowser = {};
AuditLogBrowser.ENDPOINT = "api/-default-/public/alfresco/versions/1/audit-applications/share-site-access/audit-entries";
AuditLogBrowser.MAX_ITEMS = 100;
AuditLogBrowser.POPUP_HIDE_DELAY = 3000;
AuditLogBrowser.param = {
  include: "values",
  fields: "pagination,id,createdAt,createdByUser,values",
  orderBy: "createdAt DESC",
  skipCount: 0,
  maxItems: AuditLogBrowser.MAX_ITEMS
};
AuditLogBrowser.COLUMN_DEFINITION = [{
    key: "entry.id",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.id"),
    resizeable: true
  },
  {
    key: "entry.createdAt",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.date"),
    formatter:"auditDateFormatter",
    sortable: true
  },
  {
    key: "entry.user",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.user"),
    sortable: true
  },
  {
    key: "entry.action",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.action")
  },
  {
    key: "entry.site",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.site")
  },
  {
    key: "entry.nodename",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.content")
  },
  {
    key: "entry.nodepath",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.path"),
    resizeable: true
  },
  {
    key: "entry.description",
    label: Alfresco.util.message("Ricksoft.auditlogbrowser.list.label.description"),
    resizeable: true
  }
];
AuditLogBrowser.configs = {
  draggableColumns: true
}
AuditLogBrowser.RESPONSE_SCHEMA = {
 resultsList: "list.entries",
 fields: [
   { key: "entry.id" },
   { key: "entry.createdAt", parser: "date" },
   { key: "entry.user" },
   { key: "entry.action" },
   { key: "entry.site" },
   { key: "entry.nodename" },
   { key: "entry.nodepath" },
   { key: "entry.description" }
 ]
};

YAHOO.widget.DataTable.Formatter.auditDateFormatter = function(elLiner, oRecord, oColumn, oData) {
  if(oRecord.getData("entry.createdAt")){
    elLiner.innerHTML = YAHOO.util.Date.format(oData, {format: "%Y-%m-%d %T %Z"});
  }
};

$(function(){

  getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);

  $('#search-audit-log').on('click',function(){

    var paramUser     = $('#executingUser').val();
    var paramContent  = $('#contentValue').val();
    var paramFromdate = $('#fromDate').val();
    var paramFromtime = $('#fromTime').val();
    var paramTodate   = $('#toDate').val();
    var paramTotime   = $('#toTime').val();

    if (haveOnlyTimeInput(paramFromdate, paramFromtime)
     || haveOnlyTimeInput(paramTodate, paramTotime)) {
       Alfresco.util.PopupManager.displayMessage({
         text: Alfresco.util.message("Ricksoft.auditlogbrowser.alert.message.nodateinput")
       });
       return;
    } else if (generateFromDate(paramFromdate, paramFromtime) > generateToDate(paramTodate, paramTotime)) {
      Alfresco.util.PopupManager.displayMessage({
        text: Alfresco.util.message("Ricksoft.auditlogbrowser.alert.message.reverseorder")
      });
      return;
    }

    AuditLogBrowser.param.skipCount = 0;
    delete AuditLogBrowser.param.where;

    var whereParams = createWhereParams(paramUser, paramContent, paramFromdate, paramFromtime, paramTodate, paramTotime);

    if (whereParams) {
      AuditLogBrowser.param.where = "(" + whereParams + ")";
    }

    getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);
  });

  $('#download-audit-log').on('click',function(){
    var downloadURL = "/DownloadAuditLogZip";
    var paramUser     = $('#executingUser').val();
    var paramContent  = $('#contentValue').val();
    var paramFromdate = $('#fromDate').val();
    var paramFromtime = $('#fromTime').val();
    var paramTodate   = $('#toDate').val();
    var paramTotime   = $('#toTime').val();

    if (haveOnlyTimeInput(paramFromdate, paramFromtime)
     || haveOnlyTimeInput(paramTodate, paramTotime)) {
       Alfresco.util.PopupManager.displayMessage({
         text: Alfresco.util.message("Ricksoft.auditlogbrowser.alert.message.nodateinput")
       });
       return;
    } else if (generateFromDate(paramFromdate, paramFromtime) > generateToDate(paramTodate, paramTotime)) {
      Alfresco.util.PopupManager.displayMessage({
        text: Alfresco.util.message("Ricksoft.auditlogbrowser.alert.message.reverseorder")
      });
      return;
    }

    var input = {
      user: $('#executingUser').val(),
      content: $('#contentValue').val(),
      fromDate: $('#fromDate').val(),
      fromTime: $('#fromTime').val(),
      toDate: $('#toDate').val(),
      toTime: $('#toTime').val()
    }

    document.location.href = Alfresco.constants.PROXY_URI + downloadURL + "?" + $.param(input);

  });

  $('#confirm-del-audit').on('click', function(event){
    // フォームの内容を取得
    var form = document.getElementById('del-form');

    if (form.checkValidity()) {
      var delFromdate = $('#del-from-date').val();
      var delFromtime = $('#del-from-time').val();
      var delTodate   = $('#del-to-date'  ).val();
      var delTotime   = $('#del-to-time'  ).val();
      setOutputValues(delFromdate, delFromtime, delTodate, delTotime);

      $('#input-del-term' ).hide();
      $('#output-del-term').show();
      $(this).hide();
      $('#submit-del-audit').show();

    } else {
      event.preventDefault();
      event.stopPropagation();
    }

    form.classList.add('was-validated');

  });

  $('#submit-del-audit').on('click', function(){
    var delFromdate = $('#del-from-date').val();
    var delFromtime = $('#del-from-time').val();
    var delTodate   = $('#del-to-date'  ).val();
    var delTotime   = $('#del-to-time'  ).val();
    var delTerm = {
      where: "(" + createWhereParams("", "", delFromdate, delFromtime, delTodate, delTotime) + ")"
    };
    $('.alert.alert-primary').show();
    deleteAuditLogs(AuditLogBrowser.ENDPOINT + "?" + $.param(delTerm));
  });

  $('#delAuditModal').on({
    'hide.bs.modal': function(){
      $('#input-del-term').show();
      $('.alert').hide();
      $('#output-del-term').hide();
      $('#submit-del-audit').hide();
      $('#confirm-del-audit').show();
    },
    'hidden.bs.modal': function(){
      var form = document.getElementById('del-form');
      form.classList.remove('was-validated');

      $('#del-from-date').val('');
      $('#del-from-time').val('');
      $('#del-to-date'  ).val('');
      $('#del-to-time'  ).val('');
      setOutputValues("", "", "", "");
    }
  });

  $('#prev-page').on('click',function(){
    AuditLogBrowser.param.skipCount -= AuditLogBrowser.MAX_ITEMS;
    getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);
  });
  $('#next-page').on('click',function(){
    AuditLogBrowser.param.skipCount +=  AuditLogBrowser.MAX_ITEMS;
    getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);
  });

});

/**
 * Where condition Setting
 */
function createWhereParams(user, contentName, fromDate, fromTime, toDate, toTime) {
   var params = {};

   // User
   if (user) {
     params.createdByUser = "'" + user + "'";
   }

   // Content
   if (contentName) {
     params.valuesKey   = "'/share-site-access/transaction/nodename'";
     params.valuesValue = "'" + contentName + "'";
   }

   // Period
   if (fromDate || toDate) {
     var periods = [];
     periods.push(generateFromDate(fromDate, fromTime).toISOString());
     periods.push(generateToDate(toDate, toTime).toISOString());

   }

   if (Object.entries(params)) {
     var paramList = Object.entries(params).map(function(entry){
        return entry.join("=");
     });
   }

   if (periods) {
     paramList.push("createdAt BETWEEN('" + periods.join("','") + "')");
   }

   return paramList.join(' and ');
 }

/**
 * Get Audit Logs method
 */
function getAuditLogs(endpointURL,data){

  Alfresco.util.Ajax.jsonGet({
      url: Alfresco.constants.URL_CONTEXT + endpointURL,
      dataObj: data,
      successCallback: {
         fn: function (result) {
           // ページング系処理
           var pagination = result.json["list"]["pagination"];
           $('#prev-page').prop('disabled', !Boolean(pagination.skipCount));
           $('#next-page').prop('disabled', !pagination.hasMoreItems);
           $('#download-audit-log').prop('disabled', false);
           $('#delete-audit-log').prop('disabled', false);

           result.json.list.entries = settleEntries(result.json.list.entries);

           // データテーブル形処理
           YAHOO.example.Data = result.json;
           var dataSource = new YAHOO.util.DataSource(YAHOO.example.Data);
           dataSource.responseType   = YAHOO.util.DataSource.TYPE_JSON;
           dataSource.responseSchema = AuditLogBrowser.RESPONSE_SCHEMA;

           var dataTable = new YAHOO.widget.DataTable("audit-log-table", AuditLogBrowser.COLUMN_DEFINITION, dataSource, AuditLogBrowser.configs);

           // ソート情報
           dataTable.subscribe("theadCellClickEvent", function (oArgs) {
             var uiSortedBy = this.getState().sortedBy;
             delete uiSortedBy.column;
             AuditLogBrowser.configs.sortedBy = uiSortedBy;
           });

         },
         scope: this
      },
      failureCallback: {
         fn: function (res) {
           $('#download-audit-log').prop('disabled', true);
           $('#delete-audit-log').prop('disabled', true);
           $('#prev-page').prop('disabled', true);
           $('#next-page').prop('disabled', true);
           $('.alert.alert-danger.data-error').show();
         },
         scope: this
      }
   });
}

/**
 * Delete Audit Logs method
 */
function deleteAuditLogs(endpointURL){
  Alfresco.util.Ajax.jsonDelete({
      url: Alfresco.constants.URL_CONTEXT + endpointURL,
      successCallback: {
         fn: function (result) {
           $('.alert.alert-primary').hide();
           $('.alert.alert-success').show();
           setTimeout(function(){
             $('#delAuditModal').modal('hide');
           }, AuditLogBrowser.POPUP_HIDE_DELAY);
         },
         scope: this
      },
      failureCallback: {
         fn: function () {
           $('.alert.alert-primary').hide();
           $('.alert.alert-danger').show();
         },
         scope: this
      }
   });
}

function settleEntries(logs) {
  logs.forEach(function (log, i) {
    var permInfo = {};
    var descArray = [];

    Object.keys(log.entry.values).forEach(function (key) {

      if (key.endsWith("user")
       || key.endsWith("action")
       || key.endsWith("site")
       || key.endsWith("nodepath")) {
        log.entry[key.slice(key.lastIndexOf("/")+1)] = log.entry.values[key];
      } else if (key.endsWith("nodename")) {
        log.entry[key.slice(key.lastIndexOf("/")+1)] = settleUl(String(log.entry.values[key]).split(","));
      } else {
        if (key.endsWith("authority")) {
          permInfo["authority"] = log.entry.values[key];
        } else if (key.endsWith("permission")) {
          permInfo["permission"] = log.entry.values[key];
        } else if (key.endsWith("role")) {
          permInfo["role"] = log.entry.values[key];
        } else if (key.endsWith("description")){
          permInfo["description"] = log.entry.values[key];
        }

        if (permInfo.hasOwnProperty("authority") && permInfo.hasOwnProperty("permission")) {
          descArray.push(permInfo["authority"] + " : " + permInfo["permission"]);
        } else if (permInfo.hasOwnProperty("role") && permInfo.hasOwnProperty("description")) {
          descArray.push(permInfo["role"] + " : " + permInfo["description"]);
        }

        if (typeof log.entry.values[key] === 'object') {

          if (Array.isArray(log.entry.values[key])) {
            descArray.push(log.entry.values[key]);
          } else {
            descArray.push(JSON.stringify(log.entry.values[key]));
          }
        } else {
          descArray.push(log.entry.values[key]);
        }
      }
    });

    log.entry.description = settleUl(descArray);

  });

  return logs;
}

function settleProperties() {

}

// format to unordered list
function settleUl(elems){
  var ul = document.createElement("ul");
  elems.forEach(function(el){
    var li = document.createElement("li");
    var newItem = document.createTextNode(el);
    li.appendChild(newItem);
    ul.appendChild(li);
  });

  return ul.outerHTML;
}

function setOutputValues(fromDate, fromTime, toDate, toTime) {
  $("[name=output-from-date]").val(fromDate);
  $("[name=output-from-time]").val(fromTime);
  $("[name=output-to-date]"  ).val(toDate);
  $("[name=output-to-time]"  ).val(toTime);
}

// check Whether only time is set
function haveOnlyTimeInput(tDate, tTime){
  return (!tDate && tTime);
}

// generate Date Object from fromDate and fromTime input
function generateFromDate(tDate, tTime) {
  if (!tDate) {
    return new Date(1);
  } else if (!tTime) {
    return new Date(tDate);
  } else {
    return new Date(tDate + " " + tTime);
  }
}

// generate Date Object from toDate and toTime input
function generateToDate(tDate, tTime) {
  if (!tDate) {
    return new Date();
  } else if (!tTime) {
    return new Date(tDate + " 23:59:59");
  } else {
    return new Date(tDate + " " + tTime);
  }
}
