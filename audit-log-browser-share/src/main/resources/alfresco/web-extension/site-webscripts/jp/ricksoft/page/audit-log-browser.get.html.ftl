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
        <span class="form-part">
          <span class="form-part-left">
            <div>
              <label for="fromDate">${msg("form.label.from")}</label>
              <span class="form-part">
                <div>
                  <input type="date" name="fromDate" id="fromDate"/>
                </div>
                <div>
                  <input type="time" name="fromTime" id="fromTime"/>
                </div>
              </span>
            </div>
            <div>
              <label for="executingUser">${msg("form.label.user")}</label>
              <input type="text" id="executingUser">
            </div>
          </span>
          <span class="form-part-center">
            <div>
              <label for="toDate">${msg("form.label.to")}</label>
              <span class="form-part">
                <div>
                  <input type="date" name="toDate" id="toDate"/>
                </div>
                <div>
                  <input type="time" name="toTime" id="toTime"/>
                </div>
              </span>
            </div>
            <div>
              <label for="contentValue">${msg("form.label.content")}</label>
              <input type="text" name="contentValue" id="contentValue"/>
            </div>
          </span>
        </span>
        <span>
          <span class="yui-button alf-primary-button form-part">
            <button id="search-audit-log">${msg("button.form.search")}</button>
          </span>
          <span class="yui-button alf-primary-button form-part">
            <button id="download-audit-log">${msg("button.form.download")}</button>
          </span>
          <span class="yui-button alf-primary-button form-part">
            <button id="delete-audit-log">${msg("button.form.delete")}</button>
          </span>
        </span>
      </form>
    </div>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div class="yui-skin-lightTheme">
      <span class="yui-button alf-primary-button">
        <button id="prev-page">${msg("button.paging.previous")}</button>
      </span>
      <span class="yui-button alf-primary-button">
        <button id="next-page">${msg("button.paging.next")}</button>
      </span>
    </div>
  </@>
</@>
