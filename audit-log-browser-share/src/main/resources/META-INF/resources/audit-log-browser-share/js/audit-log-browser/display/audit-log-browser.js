/*-
 * #%L
 * Audit Log Browser Share JAR Module
 * %%
 * Copyright (C) 2018 - 2020 Ricksoft Co., Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
var AuditLogBrowser = {};
AuditLogBrowser.ENDPOINT = "proxy/alfresco-api/-default-/public/alfresco/versions/1/audit-applications/alb-share-access/audit-entries";
AuditLogBrowser.ENDPOINT_CANCEL = "proxy/alfresco/audit/clearProcess";
AuditLogBrowser.MAX_ITEMS = 100;
AuditLogBrowser.POPUP_HIDE_DELAY = 3000;
AuditLogBrowser.param = {
    include: "values",
    fields: "pagination,id,createdAt,createdByUser,values",
    orderBy: "createdAt DESC",
    skipCount: 0,
    maxItems: AuditLogBrowser.MAX_ITEMS
};
AuditLogBrowser.COLUMN_DEFINITION = [
    {
        key: "entry.id",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.id"),
        resizeable: true
    },
    {
        key: "entry.createdAt",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.date"),
        formatter: "auditDateFormatter",
        sortable: true
    },
    {
        key: "entry.user",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.user"),
        sortable: true
    },
    {
        key: "entry.action",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.action")
    },
    {
        key: "entry.site",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.site")
    },
    {
        key: "entry.nodename",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.content")
    },
    {
        key: "entry.nodepath",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.path"),
        resizeable: true
    },
    {
        key: "entry.description",
        label: Alfresco.util.message("Ricksoft.audit-log-browser.list.label.description"),
        resizeable: true
    }
];
AuditLogBrowser.configs = {
    draggableColumns: true
}
AuditLogBrowser.RESPONSE_SCHEMA = {
    resultsList: "list.entries",
    fields: [
        {key: "entry.id"},
        {key: "entry.createdAt", parser: "date"},
        {key: "entry.user"},
        {key: "entry.action"},
        {key: "entry.site"},
        {key: "entry.nodename"},
        {key: "entry.nodepath"},
        {key: "entry.description"}
    ]
};

AuditLogBrowser.checkProcessId;
AuditLogBrowser.isInProgress = false;
AuditLogBrowser.pid = "";

YAHOO.widget.DataTable.Formatter.auditDateFormatter = function (elLiner, oRecord, oColumn, oData) {
    if (oRecord.getData("entry.createdAt")) {
        elLiner.innerHTML = YAHOO.util.Date.format(oData, {format: "%Y-%m-%d %T %Z"});
    }
};

$(function () {
    var paramUser, paramContent, paramFromdate, paramFromtime, paramTodate, paramTotime;
    var delFromdate, delFromtime, delTodate, delTotime;

    getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);

    $('.modal').modaal();

    $('#search-audit-log').on('click', function () {
        paramUser = $('#executingUser').val();
        paramContent = $('#contentValue').val();
        paramFromdate = $('#fromDate').val();
        paramFromtime = $('#fromTime').val();
        paramTodate = $('#toDate').val();
        paramTotime = $('#toTime').val();

        if (haveOnlyTimeInput(paramFromdate, paramFromtime)
            || haveOnlyTimeInput(paramTodate, paramTotime)) {
            Alfresco.util.PopupManager.displayMessage({
                text: Alfresco.util.message("Ricksoft.audit-log-browser.alert.message.nodateinput")
            });
            return;
        } else if (generateFromDate(paramFromdate, paramFromtime) > generateToDate(paramTodate, paramTotime)) {
            Alfresco.util.PopupManager.displayMessage({
                text: Alfresco.util.message("Ricksoft.audit-log-browser.alert.message.reverseorder")
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

    $('#download-audit-log').on('click', function () {

        if (AuditLogBrowser.isInProgress) {
            return;
        }

        $('#dl-in-progress').show();
        $('#dl-finish').hide();
        $('#download-audit-log').prop('disabled', true);

        AuditLogBrowser.pid = crypto.randomUUID();

        var downloadURL = "/DownloadAuditLogZip";

        if (haveOnlyTimeInput(paramFromdate, paramFromtime)
            || haveOnlyTimeInput(paramTodate, paramTotime)) {
            Alfresco.util.PopupManager.displayMessage({
                text: Alfresco.util.message("Ricksoft.audit-log-browser.alert.message.nodateinput")
            });
            return;
        } else if (generateFromDate(paramFromdate, paramFromtime) > generateToDate(paramTodate, paramTotime)) {
            Alfresco.util.PopupManager.displayMessage({
                text: Alfresco.util.message("Ricksoft.audit-log-browser.alert.message.reverseorder")
            });
            return;
        }

        var input = createDownloadParams(
            paramUser,
            paramContent,
            paramFromdate,
            paramFromtime,
            paramTodate,
            paramTotime,
            AuditLogBrowser.pid
        );

        AuditLogBrowser.isInProgress = true;
        AuditLogBrowser.checkProcessId = setInterval(getExportStatus, 5000);

        Alfresco.util.Ajax.jsonPost({
            url: Alfresco.constants.PROXY_URI + downloadURL,
            dataObj: input,
            successCallback: {
                fn: function (result) {
                    console.log(result.json);
                },
                scope: this
            },
            failureCallback: {
                fn: function (res) {
                    console.log(res);
                    AuditLogBrowser.isInProgress = false;
                    clearInterval(AuditLogBrowser.checkProcessId);
                },
                scope: this
            }
        });

    });

    $('#confirm-del-audit').on('click', function (event) {
        // get form date
        var form = document.getElementById('del-form');

        if (form.checkValidity()) {
            delFromdate = $('#del-from-date').val();
            delFromtime = $('#del-from-time').val();
            delTodate = $('#del-to-date').val();
            delTotime = $('#del-to-time').val();
            setOutputValues(delFromdate, delFromtime, delTodate, delTotime);

            $('#input-del-term').hide();
            $('#output-del-term').show();
            $(this).hide();
            $('#submit-del-audit').show();

        } else {
            event.preventDefault();
            event.stopPropagation();
        }

        form.classList.add('was-validated');

    });

    $('#submit-del-audit').on('click', function () {
        var delTerm = {
            where: "(" + createWhereParams("", "", delFromdate, delFromtime, delTodate, delTotime) + ")"
        };
        $('.alert.alert-primary').show();
        deleteAuditLogs(AuditLogBrowser.ENDPOINT + "?" + $.param(delTerm));
    });

    $('#delAuditModal').on({
        'hide.bs.modal': function () {
            $('#input-del-term').show();
            $('.alert').hide();
            $('#output-del-term').hide();
            $('#submit-del-audit').hide();
            $('#confirm-del-audit').show();
        },
        'hidden.bs.modal': function () {
            $('#del-form').classList.remove('was-validated');

            $('#del-from-date').val('');
            $('#del-from-time').val('');
            $('#del-to-date').val('');
            $('#del-to-time').val('');
            setOutputValues("", "", "", "");
        }
    });

    $('#prev-page').on('click', function () {
        AuditLogBrowser.param.skipCount -= AuditLogBrowser.MAX_ITEMS;
        getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);
    });
    $('#next-page').on('click', function () {
        AuditLogBrowser.param.skipCount += AuditLogBrowser.MAX_ITEMS;
        getAuditLogs(AuditLogBrowser.ENDPOINT, AuditLogBrowser.param);
    });
    $('#delete-audit-log').on('click', function () {
        $('#delAuditModal').show();
    });

    $(window).on('beforeunload', function (e) {
        if (AuditLogBrowser.isInProgress) {
            e.preventDefault();
        }
    });

    $(window).on('unload', function () {
        if (AuditLogBrowser.isInProgress) {
            onCloseWindow(AuditLogBrowser.pid);
        }
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
        params.valuesKey = "'/alb-share-access/transaction/nodename'";
        params.valuesValue = "'" + contentName + "'";
    }

    // Period
    if (fromDate || toDate) {
        var periods = [];
        periods.push(generateFromDate(fromDate, fromTime).toISOString());
        periods.push(generateToDate(toDate, toTime).toISOString());

    }

    if (Object.entries(params)) {
        var paramList = Object.entries(params).map(function (entry) {
            return entry.join("=");
        });
    }

    if (periods) {
        paramList.push("createdAt BETWEEN('" + periods.join("','") + "')");
    }

    return paramList.join(' and ');
}

function createDownloadParams(user, contentName, fromDate, fromTime, toDate, toTime, pid) {
    var params = {};

    // User
    if (user) {
        params.createdByUser = user;
    }

    // Content
    if (contentName) {
        params.valuesKey = "'/alb-share-access/transaction/nodename'";
        params.valuesValue = contentName;
    }

    // fromDate
    if (fromDate) {
        params.fromDate = fromDate;
    }
    // fromTime
    if (fromTime) {
        params.fromTime = fromTime;
    }

    // toDate
    if (toDate) {
        params.toDate = toDate;
    }

    // toTime
    if (toTime) {
        params.toTime = toTime;
    }

    // pid
    if (pid) {
        params.pid = pid;
    }

    return params;

}

/**
 * Get Audit Logs method
 */
function getAuditLogs(endpointURL, data) {

    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.URL_CONTEXT + endpointURL,
        dataObj: data,
        successCallback: {
            fn: function (result) {
                // Paging
                var pagination = result.json["list"]["pagination"];
                $('#prev-page').prop('disabled', !Boolean(pagination.skipCount));
                $('#next-page').prop('disabled', !pagination.hasMoreItems);
                $('#download-audit-log').prop('disabled', false);
                $('#delete-audit-log').prop('disabled', false);

                result.json.list.entries = settleEntries(result.json.list.entries);

                // Prepare datatable
                YAHOO.example.Data = result.json;
                var dataSource = new YAHOO.util.DataSource(YAHOO.example.Data);
                dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
                dataSource.responseSchema = AuditLogBrowser.RESPONSE_SCHEMA;

                var dataTable = new YAHOO.widget.DataTable("audit-log-table", AuditLogBrowser.COLUMN_DEFINITION, dataSource, AuditLogBrowser.configs);

                // Datatable sort info
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
 * Get Audit export status
 */
function getExportStatus() {
    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + '/audit/export/status?pid=' + AuditLogBrowser.pid,
        successCallback: {
            fn: function (result) {
                console.log(result.json.exportStatus);
                if (result.json.exportStatus === 'Finished' || result.json.exportStatus === 'Failure') {
                    $('#dl-in-progress').hide();
                    $('#dl-finish').show();
                    if (result.json.zipFileRef) {
                        $('#dl-link-button')[0].setAttribute('onClick', 'window.open("' + Alfresco.constants.URL_PAGECONTEXT + 'document-details?nodeRef=' + result.json.zipFileRef + '")');
                    }
                    clearInterval(AuditLogBrowser.checkProcessId);
                    AuditLogBrowser.isInProgress = false;
                    $('#download-audit-log').prop('disabled', false);
                }
            },
            scope: this
        },
        failureCallback: {
            fn: function (res) {
                console.log(res);
                clearInterval(AuditLogBrowser.checkProcessId);
                AuditLogBrowser.isInProgress = false;
                $('#download-audit-log').prop('disabled', false);
            },
            scope: this
        }
    });
}

/**
 * Delete Audit Logs method
 */
function deleteAuditLogs(endpointURL) {
    Alfresco.util.Ajax.jsonDelete({
        url: Alfresco.constants.URL_CONTEXT + endpointURL,
        successCallback: {
            fn: function (result) {
                $('.alert.alert-primary').hide();
                $('.alert.alert-success').show();
                setTimeout(function () {
                    $('#delAuditModal').hide();
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
                log.entry[key.slice(key.lastIndexOf("/") + 1)] = log.entry.values[key];
            } else if (key.endsWith("nodename")) {
                log.entry[key.slice(key.lastIndexOf("/") + 1)] = settleUl(String(log.entry.values[key]).split(","));
            } else {
                if (key.endsWith("authority")) {
                    permInfo["authority"] = log.entry.values[key];
                } else if (key.endsWith("permission")) {
                    permInfo["permission"] = log.entry.values[key];
                } else if (key.endsWith("role")) {
                    permInfo["role"] = log.entry.values[key];
                } else if (key.endsWith("description")) {
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

// format to unordered list
function settleUl(elems) {
    var ul = document.createElement("ul");
    elems.forEach(function (el) {
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
    $("[name=output-to-date]").val(toDate);
    $("[name=output-to-time]").val(toTime);
}

// check Whether only time is set
function haveOnlyTimeInput(tDate, tTime) {
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

// request to cancel dl process
function onCloseWindow(pid) {
    navigator.sendBeacon(Alfresco.constants.URL_CONTEXT + AuditLogBrowser.ENDPOINT_CANCEL + "?pid=" + pid);
}
