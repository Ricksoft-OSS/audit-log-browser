package auditlogbrowser.schedule;

import static org.junit.Assert.fail;

import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import jp.ricksoft.auditlogbrowser.alfresco.schedule.AuditlogArchiveScheduler;

@RunWith(value = AlfrescoTestRunner.class)
public class AuditlogArchiveSchedulerTest extends AbstractAlfrescoIT {
    private AuditlogArchiveScheduler scheduler = (AuditlogArchiveScheduler) getApplicationContext()
            .getBean("jp.ricksoft.schedule.AuditlogArchiveScheduler");

    @Test
    public void executeScheduledJob() {
        try {
            scheduler.execute();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
