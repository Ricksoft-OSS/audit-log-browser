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
<@processJsonModel />
<@markup id="html">
  <@uniqueIdDiv>
    <div class="audit-log-browser">
      <form action="${url.context}/page/console/admin-console/audit-log-browser" method="get" class="yui-skin-lightTheme">
        <span>
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
          <div>
            <div class="form-part">
              <label for="executingUser">${msg("form.label.user")}</label>
              <input type="text" id="executingUser">
            </div>
            <div class="form-part">
              <label for="contentValue">${msg("form.label.content")}</label>
              <input type="text" name="contentValue" id="contentValue"/>
            </div>
          </div>
        </span>
        <span class="yui-button alf-primary-button form-part">
          <button id="search-audit-log">${msg("button.form.search")}</button>
          <button id="download-audit-log">${msg("button.form.download")}</button>
          <button id="delete-audit-log">${msg("button.form.delete")}</button>
        </span>
      </form>
    </div>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div class="yui-skin-lightTheme">
      <span class="yui-button alf-primary-button">
        <button id="prev-page">${msg("button.paging.previous")}</button>
        <button id="next-page">${msg("button.paging.next")}</button>
      </span>
    </div>
  </@>
</@>
