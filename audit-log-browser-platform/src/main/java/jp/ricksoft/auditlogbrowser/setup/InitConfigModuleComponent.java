package jp.ricksoft.auditlogbrowser.setup;

import org.alfresco.repo.module.AbstractModuleComponent;

import java.io.File;

public class InitConfigModuleComponent extends AbstractModuleComponent {

    private String tmpDirPath;

    public void setTmpDirPath(String tmpDirPath) {
        this.tmpDirPath = tmpDirPath;
    }

    @Override
    protected void executeInternal() {
        File dir = new File(tmpDirPath);

        if (dir.exists())
        {
        } else {
            dir.mkdir();
        }

    }

}
