package jp.ricksoft.auditlogbrowser.alfresco.setup;

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

import org.alfresco.repo.module.AbstractModuleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class InitConfigModuleComponent extends AbstractModuleComponent {
    private static final Logger LOG = LoggerFactory.getLogger(InitConfigModuleComponent.class);

    @Value("${AuditLogBrowser.dir.tmp}")
    private String tmpDirPath;

    @Override
    protected void executeInternal() {
        File dir = new File(tmpDirPath);

        if (dir.exists())
        {
            LOG.info("Tmp directory '{}' has already exist. Skipping.", tmpDirPath);
        } else {
            LOG.info("Creating tmp directory '{}'.", tmpDirPath);
            dir.mkdir();
            LOG.info("Created tmp directory '{}'.", tmpDirPath);
        }

    }

}
