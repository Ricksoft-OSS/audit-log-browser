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
        <button id="search-audit-log"   value="${msg("button.form.search")}">
        <button id="download-audit-log" value="${msg("button.form.download")}">
        <button id="delete-audit-log"   value="${msg("button.form.delete")}">
      </form>
    </div>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div>
      <input id="prev-page" type="button" value="${msg("button.paging.previous")}">
      <input id="next-page" type="button" value="${msg("button.paging.next")}">
    </div>
  </@>
</@>
