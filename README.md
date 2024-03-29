

# Audit Log Browser

### What is Audit Log Browser?

Audit Log Browser is an add-on that allows you to view the Alfresco Content Services audit log from the browser.

### Description

Alfresco's audit log is stored in Alfresco's database. However, there is no user-friendly way to see stored logs. Audit log Browser adds the function to display the audit log to Alfresco Content Services management console, and can check from the browser.
For more information about features, see **Specifications** section.

### Installation

1. Download jar file from [GitHub Release page](https://github.com/Ricksoft-OSS/audit-log-browser/releases).
    1. audit-log-browser-platform-x.y.z.jar
    2. audit-log-browser-share-x.y.z.jar
2. Send jar files to Alfresco server.
3. Place audit-log-browser-platform-x.y.z.jar to ＜Alfresco Content Services Installation Directory＞/module/platform directory. If the directory doesn't exist, create it before place jar file.
4. Place audit-log-browser-share-x.y.z.jar to ＜Alfresco Content Services Installation Directory＞/module/share directory. If the directory doesn't exist, create it before place jar file.
5. Change owner of platform, share directory, and placed jar files to Alfresco Content Services execution user.
6. Restart Alfresco Content Services.

### Configuration

You can set the following properties in alfresco-global.properties file. The default values are as shown in the table.

|Setting contents|Property key|Default|
|--------|--------------|------------|
|Enable Schedule function |AuditLogBrowser.schedule.enabled|true|
|Enable Delete function at schedule setting|AuditLogBrowser.schedule.delete.enabled|false|
|Schedule processing execution timing |AuditLogBrowser.schedule.cron.expression|0 0 * * * ?|
|Time from the start of the ACS instance to the start of the scheduler (milli second)|AuditLogBrowser.schedule.cron.start.delay|240000|
|Retention period for Audit log (day)|AuditLogBrowser.schedule.archive.storage.period|7|

### Specifications

In this add-on, you can use the following features.

1. Browse audit logs.
2. Search logs by specifying search criteria.
3. Delete logs.
4. Scheduler(Archive・Delete)
5. Download.

Following information in audit logs can be displayed.

- Login/Logout/Login Failure
- Content preview/download (these are displayed as the same event)
- Create/Delete folder
- Copy/Move contents
- Check-in/Check-out/Cancel check-out contents
- Link Folder
- Add/Delete properties
- Add/Delete aspect
- Create/update/delete users
- Change authority on content
- Change authority of things other than contents
- Change site privileges
- Setting of document owner
- Document type after change
- Create/delete/change group

### Limitations

1. Please use Firefox or Google Chrome when you use this add-on.
2. On the Browser, maximum of 100 audit logs are displayed at a time.
3. In the search function, when "start date" is not set, it is not used as a search condition even if "start time" is set (it is regarded as unset state).
4. In the search function, when "end date" is not set, it is not used as a search condition even if "end time" is set (it is regarded as not set).
5. With the search function, search criteria of users and contents must be exact match.
6. In the deletion function, if either the start date or the end date is not set, it can not be deleted. When you delete audit logs, you must set "start date" and "end date".
7. The scheduled deletion function assumes that the schedule function must be set. When the schedule function isn't set, the scheduled deletion function doesn't work.
8. With the schedule function, the audit log is saved as a Zip file in the shared repository after 7 days since it is recorded. At that time, if the deletion function at schedule setting is set to on, the audit log is deleted from the database.
9. If the amount of logs is large in the download function, there is a possibility that the performance of the server will be degraded and the timeout will result. By using the function to store the audit log in the repository, it is possible to download the compressed audit log separately every day.

### Contribution

If you would like to request bug fixes and additional functions, please create Issue or Pull Request in Github.

### Credit

- Yuuki Ebihara (ebihara.yuki@ricksoft.jp)

### License

Copyright 2018 Ricksoft Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
