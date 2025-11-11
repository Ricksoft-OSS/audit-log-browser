package jp.ricksoft.auditlogbrowser.alfresco.schedule;

/*-
 * #%L
 * Audit Log Browser Platform JAR Module
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

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import jp.ricksoft.auditlogbrowser.service.AuditLogFileService;
import jp.ricksoft.auditlogbrowser.util.DateUtil;

public class AuditlogArchiveScheduler {

//  private static final String NAME_DAILYZIP = "Auditlogs_%s.zip";
  private static final String MSG_NO_BACKUP_DIRECTORY = "No backup directory set.";
  private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final Logger LOG = LoggerFactory.getLogger(AuditlogArchiveScheduler.class);

  @Value("${AuditLogBrowser.schedule.delete.enabled}")
  private boolean isDeleteEnabled;

  @Value("${AuditLogBrowser.schedule.archive.storage.period}")
  private int retentionPeriod;

  @Value("${AuditLogBrowser.daily.zip.name}")
  private String dailyZipFileName;

  @Value("${AuditLogBrowser.debug}")
  private boolean debug;

  private AuditLogFileService auditLogFileService;

  public void setAuditLogFileService(AuditLogFileService auditLogFileService) {
    this.auditLogFileService = auditLogFileService;
  }

  /** Executer implementation */
  public void execute() {

    LOG.debug("============ Start Schedule Archive.");

    // Retention periods < 1 is NOT RECOMMENDED because it can cause unexpected behaviour.
    // If you must set the parameter as it, you should execute this as debug mode.
    if(!debug && retentionPeriod < 1){
        LOG.error("Setting a retention period less than 1 is not permitted.: {}", retentionPeriod);
        return;
    }

    final String processId = String.valueOf(UUID.randomUUID());

    // from
    LocalDate fromDate = this.auditLogFileService.getOldestLoggedDateTime().toLocalDate();
    // to
    LocalDate toDate = LocalDate.now().minusDays(retentionPeriod);
    LOG.debug("============ FromDate: {}", fromDate);
    LOG.debug("============ ToDate: {}", toDate);

    LocalDate targetDate = fromDate;

    // NOTE: "!A.isAfter(B)" and "A.isBefore(B)" is not equilibrate
    while (!targetDate.isAfter(toDate)) {
      LOG.debug("============ Loop Start {} ============", targetDate);

      String targetDateStr = targetDate.format(FORMAT_DATE.withResolverStyle(ResolverStyle.STRICT));
      long fromEpochMilli = DateUtil.generateFromEpochMilli(targetDate);
      long toEpochMilli = DateUtil.generateToEpochMilli(targetDate);

      targetDate = targetDate.plusDays(1);
//      String zipName = String.format(dailyZipFileName, targetDateStr);
      String zipName = this.buildZipFileName(this.dailyZipFileName, targetDateStr);
      String[] targetRepositoryPath = targetDateStr.split("-");

      // Check if there is already created archived file.
      if(this.auditLogFileService.prepareArchiveStoreFolder(targetRepositoryPath, zipName)){
          LOG.warn("There is already archived log. Creation skipped: {}/{}", String.join("/", targetRepositoryPath), zipName);
          continue;
      }
      LOG.debug("FILE to create: {}/{}", String.join("/", targetRepositoryPath), zipName);

      this.auditLogFileService.exportAuditLogsZipToRepo(
          fromEpochMilli,
          toEpochMilli,
          null,
          null,
          targetRepositoryPath,
          zipName,
          // set PID blank when called by scheduled jobs.
          processId);

      LOG.debug("============ Loop End ============");
    }

    if (isDeleteEnabled) {
      this.cleanUp(fromDate, toDate);
    }

    LOG.debug("============ Finish Schedule Archive.");
  }

  /**
   * @param fromDate Delete start DateTime.
   * @param toDate Delete end DateTime.
   */
  private void cleanUp(LocalDate fromDate, LocalDate toDate) {
    LOG.debug("============ Delete old audit log start");
    // Even if you delete old logs, there is no problem
    LOG.debug("============ fromDate: {}", fromDate);
    LOG.debug("============ toDate: {}", toDate);

    long fromEpochMilli = DateUtil.generateFromEpochMilli(fromDate);
    long toEpochMilli = DateUtil.generateToEpochMilli(toDate);

    this.auditLogFileService.deleteAuditLogs(fromEpochMilli, toEpochMilli);

    LOG.debug("============ Delete old audit log end");
  }

  private String buildZipFileName(String fileName, String dateStr){
      return String.format(fileName, dateStr);
  }
}
