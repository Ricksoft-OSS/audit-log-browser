<#--
  Copyright 2018 Ricksoft Co., Ltd.
  All rights reserved.
 -->
<@markup id="css">
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/audit-log-browser/normal.css"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/audit-log-browser/audit-log-browser.css"/>
</@>
<@markup id="js">
  <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/resources/audit-log-browser-share/js/lib/jquery-3.2.1.min.js"/>
  <@script type="text/javascript" src="${url.context}/res/resources/audit-log-browser-share/js/audit-log-browser/display/audit-log-browser.js"/>
</@>
<@markup id="html">
  <@uniqueIdDiv>
    <!-- Delete Modal Window -->
        <div class="modal" id="delAuditModal" tabindex="-1" role="dialog" aria-labelledby="delAuditModalLabel" aria-hidden="true">
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
    <form action="${url.context}/page/console/admin-console/audit-log-browser" method="get">
      <fieldset>
        <div class="form-part">
          <label for="fromDate">${msg("form.label.from")}</label>
          <div>
            <input type="date" name="fromDate" id="fromDate"/>
          </div>
          <div>
            <input type="time" name="fromTime" id="fromTime"/>
          </div>
        </div>
        <div class="form-part">
          <label for="toDate">${msg("form.label.to")}</label>
          <div>
            <input type="date" name="toDate" id="toDate"/>
          </div>
          <div>
            <input type="time" name="toTime" id="toTime"/>
          </div>
        </div>
      </fieldset>
      <div class="form-part">
        <div>
          <label for="executingUser">${msg("form.label.user")}</label>
          <input type="text" id="executingUser">
        </div>
        <div>
          <label for="contentValue">${msg("form.label.content")}</label>
          <input type="text" name="contentValue" id="contentValue"/>
        </div>
      </div>
      <input id="search-audit-log"   type="button" value="${msg("button.form.search")}">
      <input id="download-audit-log" type="button" value="${msg("button.form.download")}">
      <input id="delete-audit-log"   type="button" value="${msg("button.form.delete")}">
    </form>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div>
      <input id="prev-page" type="button" value="${msg("button.paging.previous")}">
      <input id="next-page" type="button" value="${msg("button.paging.next")}">
    </div>
  </@>
</@>
