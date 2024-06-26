<#--
 #%L
 Audit Log Browser Share JAR Module
 %%
 Copyright (C) 2018 - 2020 Ricksoft Co., Ltd.
 %%
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 #L%
-->
<@markup id="css">
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/lib/modaal.min.css"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/audit-log-browser/normal.css"/>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/resources/audit-log-browser-share/css/audit-log-browser/audit-log-browser.css"/>
</@>
<@markup id="js">
  <#-- JavaScript Dependencies -->
  <@script type="text/javascript" src="${url.context}/res/resources/audit-log-browser-share/js/lib/jquery.min.js"/>
  <@script type="text/javascript" src="${url.context}/res/resources/audit-log-browser-share/js/lib/modaal.min.js"/>
  <@script type="text/javascript" src="${url.context}/res/resources/audit-log-browser-share/js/audit-log-browser/display/audit-log-browser.min.js"/>
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
            <button id="search-audit-log" type="button">${msg("button.form.search")}</button>
          </span>
          <span class="yui-button alf-primary-button form-part">
            <button id="download-audit-log" type="button">${msg("button.form.download")}</button>
          </span>
          <span class="yui-button alf-primary-button form-part">
            <a href="#delAuditModal" class="modal">${msg("button.form.delete")}</a>
          </span>
        </span>
      </form>
    </div>
    <div class="download status yui-skin-lightTheme" id="dl-status-area" >
      <span id="dl-in-progress" style="display: none;">
        <img src="${url.context}/res/resources/audit-log-browser-share/image/bars-rotate-fade.svg"/>
        <p>${msg("message.download.status.inprogress")}</p>
        <p id="dl-in-progress-percentage">0%</p>
      </span>
      <span id="dl-finish" style="display: none;">
        <span class="yui-button">
          <button id="dl-link-button" class="first-child">DL link</button>
        </span>
        <p>${msg("message.download.status.finish")}</p>
      </span>
    </div>
    <div id="audit-log-table" class="table table-bordered"></div>
    <div class="yui-skin-lightTheme paging-button">
      <span class="yui-button alf-primary-button">
        <button id="prev-page">${msg("button.paging.previous")}</button>
      </span>
      <span class="yui-button alf-primary-button">
        <button id="next-page">${msg("button.paging.next")}</button>
      </span>
    </div>

    <!-- Delete Modal Window -->
    <div class="modal" id="delAuditModal" tabindex="-1" role="dialog" style="display: none;">
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
  </@>
</@>
