# Migrate audit entries from old app

## Target user

* The user upgraded this app from version 2.0 or above to version 2.1 or lower
  
## Needed actions

* Migrate audit entries recorded with the old audit-app name to new app name

## Why is it needed?

* To fix [this issue](https://github.com/Ricksoft-OSS/audit-log-browser/issues/11) we need to change an audit-app name
* The audit-app used by this app may not be unique because 'alb-share-access' is same to [official document](https://docs.alfresco.com/content-services/latest/develop/repo-ext-points/audit-log/)
* So we decided to change the name to other unique name it while handle that issue.

## How to do?

 1. Install this app(version 2.1 or below) and restart ACS
 1. Connect to your RDBMS
 1. Execute SQL query shows below(This SQL is tested on PostgreSQL 14.8)

 ```sql
UPDATE alf_audit_entry
SET audit_app_id = (
    SELECT aap.id
    FROM alf_prop_string_value apsv
    JOIN alf_prop_value apv ON apsv.id = apv.long_value
    JOIN alf_audit_app aap ON apv.id = aap.app_name_id
    WHERE apsv.string_value = 'alb-share-access'
)
WHERE audit_app_id = (
    SELECT aap.id
    FROM alf_prop_string_value apsv
    JOIN alf_prop_value apv ON apsv.id = apv.long_value
    JOIN alf_audit_app aap ON apv.id = aap.app_name_id
    WHERE apsv.string_value = 'ShareSiteAccess'
);
 ```
