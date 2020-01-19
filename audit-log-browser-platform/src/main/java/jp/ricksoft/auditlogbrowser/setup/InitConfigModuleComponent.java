package jp.ricksoft.auditlogbrowser.setup;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class InitConfigModuleComponent extends AbstractModuleComponent {
    private static final Logger LOG = LoggerFactory.getLogger(InitConfigModuleComponent.class);

    private String tmpDirPath;

    public void setTmpDirPath(String tmpDirPath) {
        this.tmpDirPath = tmpDirPath;
    }

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
