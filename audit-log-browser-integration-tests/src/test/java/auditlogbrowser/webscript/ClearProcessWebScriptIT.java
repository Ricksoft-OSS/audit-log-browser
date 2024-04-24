package auditlogbrowser.webscript;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.google.common.collect.Maps;

import jp.ricksoft.auditlogbrowser.audit.download.DownloadAuditLogZipHandler;
import jp.ricksoft.auditlogbrowser.audit.download.DownloadProcessInfo;
import jp.ricksoft.auditlogbrowser.webscript.ClearProcessWebScript;

@RunWith(value = AlfrescoTestRunner.class)
public class ClearProcessWebScriptIT extends AbstractAlfrescoIT {

    private static final Logger logger = LoggerFactory.getLogger(ClearProcessWebScriptIT.class);

    private DownloadAuditLogZipHandler mockedHandler = (DownloadAuditLogZipHandler) getApplicationContext()
            .getBean("jp.ricksoft.audit.handler");
    private ClearProcessWebScript ws = (ClearProcessWebScript) getApplicationContext()
            .getBean("webscript.jp.ricksoft.audit.ClearProcess.post");

    @Test
    public void testExecute() {
        WebScriptRequest req = Mockito.mock(WebScriptRequest.class);
        WebScriptResponse res = Mockito.mock(WebScriptResponse.class);

        doReturn("abcd").when(req).getParameter("pid");

        ConcurrentMap<String, DownloadProcessInfo> dlProcessesInProgress = Maps.newConcurrentMap();

        dlProcessesInProgress.put("abcd", new DownloadProcessInfo("abcd"));

        try {
            Field field = DownloadAuditLogZipHandler.class.getDeclaredField("dlProcessesInProgress");
            field.setAccessible(true);
            field.set(mockedHandler, dlProcessesInProgress);
            ws.execute(req, res);
            assertEquals(0L, dlProcessesInProgress.size());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
