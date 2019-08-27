<#--
  Copyright 2018 Ricksoft Co., Ltd.
  All rights reserved.
 -->
<@markup id="css">
  <#-- CSS Library -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/lib/bootstrap.min.css"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/lib/bootstrap-grid.min.css"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/lib/bootstrap-reboot.min.css"/>
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/auditlogbrowser/normal.css"/>
</@>
<@markup id="js">
   <#-- JavaScript Dependencies -->
  <script type=”text/javascript”>
    //<![CDATA[
      dojoConfig.packages.push(
        {name : ‘jquery’, location : ‘resources/audit-log-browser-share/js/lib’, main : ‘jquery-3.2.1.min’},
        {name : ‘bootstrap’, location : ‘resources/audit-log-browser-share/js/lib’, main : ‘bootstrap.bundle.min’},
        {name : ‘GetAuditLog’, location : ‘resources/audit-log-browser-share/js/auditlogbrowser/display’, main : ‘GetAuditLog’}
      );
    //]]>
  </script>
</@>
<@markup id="html">
  <@uniqueIdDiv>
    <!-- Delete Modal Window -->
    <div class="modal fade" id="delAuditModal" tabindex="-1" role="dialog" aria-labelledby="delAuditModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="delAuditModalLabel">${msg("message.popup.delete.title")}</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="alert alert-primary alert-dismissable fade" role="alert" style="display: none;">
            ${msg("message.popup.delete.status.inprogress")}
          </div>
          <div class="alert alert-success alert-dismissable fade" role="alert" style="display: none;">
            ${msg("message.popup.delete.status.success")}
          </div>
          <div class="alert alert-danger alert-dismissable fade" role="alert" style="display: none;">
            ${msg("message.popup.delete.status.failure")}
          </div>
          <form id="del-form" novalidate>
            <div class="modal-body">
              <fieldset class="form-group mr-3" id="input-del-term">
                <p>
                  ${msg("message.popup.delete.navigation.setting")}
                </p>
                <div class="form-row mb-2">
                  <label for="del-from-date">${msg("form.label.from")}</label>
                  <div class="col">
                    <input type="date" name="del-from-date" id="del-from-date" class="form-control" required/>
                    <div class="invalid-feedback">
                      ${msg("message.popup.delete.error.nostartdate")}
                    </div>
                  </div>
                  <div class="col">
                    <input type="time" name="del-from-time" id="del-from-time" class="form-control"/>
                  </div>
                </div>
                <div class="form-row">
                  <label for="del-to-date">${msg("form.label.to")}</label>
                  <div class="col">
                    <input type="date" name="del-to-date" id="del-to-date" class="form-control" required/>
                    <div class="invalid-feedback">
                      ${msg("message.popup.delete.error.noenddate")}
                    </div>
                  </div>
                  <div class="col">
                    <input type="time" name="del-to-time" id="del-to-time" class="form-control"/>
                  </div>
                </div>
              </fieldset>
              <fieldset class="form-group mr-3" id="output-del-term" style="display: none;">
                <p>
                  ${msg("message.popup.delete.navigation.confirm")}
                </p>
                <div class="form-row mb-2">
                  <label>${msg("form.label.from")}</label>
                  <div class="col">
                    <output name="output-from-date"></output>
                  </div>
                  <div class="col">
                    <output name="output-from-time"></output>
                  </div>
                </div>
                <div class="form-row">
                  <label>${msg("form.label.to")}</label>
                  <div class="col">
                    <output name="output-to-date"></output>
                  </div>
                  <div class="col">
                    <output name="output-to-time"></output>
                  </div>
                </div>
              </fieldset>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal">${msg("button.form.cancel")}</button>
              <button type="button" class="btn btn-primary" data-dismiss="alert" id="confirm-del-audit">${msg("button.form.confirm")}</button>
              <button type="button" class="btn btn-primary" data-dismiss="alert" id="submit-del-audit" style="display: none;">${msg("button.form.delete")}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <form action="${url.context}/page/console/admin-console/auditlogbrowser" method="get" class="form-inline ml-2 mt-3 mb-4">
      <fieldset class="form-group mr-3">
        <div class="form-row mb-2">
          <label for="fromDate">${msg("form.label.from")}</label>
          <div class="col">
            <input type="date" name="fromDate" id="fromDate" class="form-control"/>
          </div>
          <div class="col">
            <input type="time" name="fromTime" id="fromTime" class="form-control"/>
          </div>
        </div>
        <div class="form-row">
          <label for="toDate">${msg("form.label.to")}</label>
          <div class="col">
            <input type="date" name="toDate" id="toDate" class="form-control"/>
          </div>
          <div class="col">
            <input type="time" name="toTime" id="toTime" class="form-control"/>
          </div>
        </div>
      </fieldset>
      <div class="form-group mr-3">
        <label for="executingUser">${msg("form.label.user")}</label>
        <input type="text" id="executingUser" class="form-control select" data-s2="true">
      </div>
      <div class="form-group mr-3">
        <label for="contentValue">${msg("form.label.content")}</label>
        <input type="text" name="contentValue" id="contentValue" class="form-control"/>
      </div>
      <input id="search-audit-log"   class="btn btn-primary btn-sm mr-3" type="button" value="${msg("button.form.search")}">
      <input id="download-audit-log" class="btn btn-primary btn-sm mr-3" type="button" value="${msg("button.form.download")}">
      <input id="delete-audit-log"   class="btn btn-primary btn-sm mr-3" type="button" value="${msg("button.form.delete")}" data-toggle="modal" data-target="#delAuditModal">
    </form>
    <div class="alert alert-danger data-error" role="alert" style="display: none;">
      ${msg("message.restapi.get.result.failure")}
    </div>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div class="text-center">
      <input id="prev-page" type="button" value="${msg("button.paging.previous")}" class="btn btn-primary">
      <input id="next-page" type="button" value="${msg("button.paging.next")}" class="btn btn-primary">
    </div>
  </@>
</@>
