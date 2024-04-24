package auditlogbrowser.webscript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jp.ricksoft.auditlogbrowser.audit.download.DownloadAuditLogZipHandler;
import jp.ricksoft.auditlogbrowser.audit.download.DownloadProcessInfo;
import jp.ricksoft.auditlogbrowser.audit.download.DownloadProgress;
import jp.ricksoft.auditlogbrowser.webscript.ExportStatusWebScript;

@RunWith(value = AlfrescoTestRunner.class)
public class ExportStatusWebScriptTest extends AbstractAlfrescoIT {

    private static final Logger logger = LoggerFactory.getLogger(ExportStatusWebScriptTest.class);

    private DownloadAuditLogZipHandler handler = (DownloadAuditLogZipHandler) getApplicationContext()
            .getBean("jp.ricksoft.audit.handler");

    private ExportStatusWebScript ws = (ExportStatusWebScript) getApplicationContext()
            .getBean("webscript.jp.ricksoft.audit.ExportStatus.get");

    @Test
    public void test_process_is_in_progress_on_start() {

        final String pid = "abcd";
        final DownloadProcessInfo dlProcInfo = new DownloadProcessInfo(pid);
        Object retObj = null;
        try {
            retObj = invokeWebScript(pid, Lists.newArrayList(dlProcInfo));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (!(retObj instanceof Map)) {
            fail();
        }

        final String zipFileRef = (String) ((Map) retObj).get("zipFileRef");
        final String exportStatus = (String) ((Map) retObj).get("exportStatus");
        final Integer percentage = (Integer) ((Map) retObj).get("percentage");

        assertNull(zipFileRef);
        assertSame(DownloadProgress.STATUS_IN_PROGRESS.message(), exportStatus);
        assertSame(0, percentage);
    }

    @Test
    public void test_process_is_in_progress_on_middle() {

        final String pid = "efgh";
        final DownloadProcessInfo dlProcInfo = new DownloadProcessInfo(pid);
        dlProcInfo.setTotal(100);
        dlProcInfo.addCreatedNum(30);

        Object retObj = null;
        try {
            retObj = invokeWebScript(pid, Lists.newArrayList(dlProcInfo));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (!(retObj instanceof Map)) {
            fail();
        }

        final String zipFileRef = (String) ((Map) retObj).get("zipFileRef");
        final String exportStatus = (String) ((Map) retObj).get("exportStatus");
        final Integer percentage = (Integer) ((Map) retObj).get("percentage");

        assertNull(zipFileRef);
        assertSame(DownloadProgress.STATUS_IN_PROGRESS.message(), exportStatus);
        assertSame(30, percentage);
    }

    @Test
    public void test_process_is_finished() {

        final String pid = "ijkl";
        final String dummyRefString = "dummy://dummy/dummy";
        final DownloadProcessInfo dlProcInfo = new DownloadProcessInfo(pid);
        dlProcInfo.setTotal(100);
        dlProcInfo.addCreatedNum(100);
        dlProcInfo.setZipFileRef(new NodeRef(dummyRefString));

        Object retObj = null;
        try {
            retObj = invokeWebScript(pid, Lists.newArrayList(dlProcInfo));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (!(retObj instanceof Map)) {
            fail();
        }

        final String zipFileRef = (String) ((Map) retObj).get("zipFileRef");
        final String exportStatus = (String) ((Map) retObj).get("exportStatus");
        final Integer percentage = (Integer) ((Map) retObj).get("percentage");

        assertEquals(dummyRefString, zipFileRef);
        assertSame(DownloadProgress.STATUS_FINISHED.message(), exportStatus);
        assertSame(0, percentage);
    }

    @Test
    public void test_process_is_expected_error() {

        final String pid = "mnop";
        final DownloadProcessInfo dlProcInfo = new DownloadProcessInfo(pid);
        dlProcInfo.setFailed(true);

        Object retObj = null;
        try {
            retObj = invokeWebScript(pid, Lists.newArrayList(dlProcInfo));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (!(retObj instanceof Map)) {
            fail();
        }

        final String zipFileRef = (String) ((Map) retObj).get("zipFileRef");
        final String exportStatus = (String) ((Map) retObj).get("exportStatus");
        final Integer percentage = (Integer) ((Map) retObj).get("percentage");

        assertNull(zipFileRef);
        assertSame(DownloadProgress.STATUS_FAILURE.message(), exportStatus);
        assertSame(0, percentage);
    }

    @Test
    public void test_process_is_unexpected_error() {
        final String pid = "qrst";
        Object retObj = null;
        try {
            retObj = invokeWebScript(pid, Lists.newArrayList());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (!(retObj instanceof Map)) {
            fail();
        }

        final String zipFileRef = (String) ((Map) retObj).get("zipFileRef");
        final String exportStatus = (String) ((Map) retObj).get("exportStatus");
        final Integer percentage = (Integer) ((Map) retObj).get("percentage");

        assertNull(zipFileRef);
        assertSame(DownloadProgress.STATUS_FAILURE.message(), exportStatus);
        assertSame(0, percentage);
    }

    // ================================================
    // Test suite
    // ================================================
    private Object invokeWebScript(String pid, List<DownloadProcessInfo> infoList) throws Exception {
        WebScriptRequest req = Mockito.mock(WebScriptRequest.class);
        Status status = Mockito.mock(Status.class);

        doReturn(pid).when(req).getParameter("pid");

        ConcurrentMap<String, DownloadProcessInfo> dlProcessesInProgress = Maps.newConcurrentMap();
        infoList.forEach(info -> dlProcessesInProgress.put(info.getProcessId(), info));

        try {
            Field field = DownloadAuditLogZipHandler.class.getDeclaredField("dlProcessesInProgress");
            Method method = ExportStatusWebScript.class.getDeclaredMethod("executeImpl",
                    WebScriptRequest.class, Status.class, Cache.class);
            field.setAccessible(true);
            field.set(handler, dlProcessesInProgress);

            method.setAccessible(true);
            return method.invoke(ws, req, status, null);

        } catch (Exception e) {
            logger.error("{}", e);
            e.printStackTrace();
            throw e;
        } finally {
            handler.removeProcessInfo(pid);
        }
    }
}
