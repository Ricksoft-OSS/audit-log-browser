package auditlogbrowser.webscript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class DownloadAuditLogZipWebScriptTest extends AbstractAlfrescoIT {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAuditLogZipWebScriptTest.class);

    private static final String ACS_ENDPOINT_PROP = "acs.endpoint.path";
    private static final String ACS_DEFAULT_ENDPOINT = "http://localhost:8080/alfresco";

    private static final String ZIPFILE_PREFIX = "TEST";

    @Test
    public void testWebScript_search_all() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "all"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_from_only() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "from-only"),
                            "2010-01-01",
                            "12:00",
                            null,
                            null,
                            null,
                            null,
                            null));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_to_only() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "to-only"),
                            null,
                            null,
                            "2024-04-01",
                            "12:00",
                            null,
                            null,
                            null));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_date_only() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "date-only"),
                            "2010-01-01",
                            null,
                            "2024-04-01",
                            null,
                            null,
                            null,
                            null));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_user() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "user"),
                            null,
                            null,
                            null,
                            null,
                            "admin",
                            null,
                            null));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_content() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "content"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            "/alb-share-access/transaction/nodename",
                            "dashboard.xml"));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    @Test
    public void testWebScript_with_complex() {

        try {
            HttpResponse httpResponse = executeScript(
                    this.buildBody(
                            this.generatePid(ZIPFILE_PREFIX, "complex"),
                            "2010-01-01",
                            "12:00",
                            "2024-04-01",
                            "19:00",
                            "admin",
                            "/alb-share-access/transaction/nodename",
                            "dashboard.xml"));
            assertEquals("Incorrect HTTP Response Status", HttpStatus.SC_OK,
                    httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("{}", e);
            fail();
        }
    }

    // ================================================
    // Test suite
    // ================================================
    private HttpResponse executeScript(String bodyJson) throws IOException {
        String webscriptURL = getPlatformEndpoint() + "/service/DownloadAuditLogZip";

        // Login credentials for Alfresco Repo
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider)
                .build();) {

            HttpPost httpPost = new HttpPost(webscriptURL);

            final StringEntity entity = new StringEntity(bodyJson);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            return httpclient.execute(httpPost);
        }
    }

    private String getPlatformEndpoint() {
        final String platformEndpoint = System.getProperty(ACS_ENDPOINT_PROP);
        return StringUtils.isNotBlank(platformEndpoint) ? platformEndpoint : ACS_DEFAULT_ENDPOINT;
    }

    private String generatePid(String prefix, String postfix) {
        return Joiner.on("_").join(prefix, UUID.randomUUID().toString(), postfix);
    }

    private String buildBody(String pid, String fromDate, String fromTime, String toDate, String toTime, String user,
            String valuesKey, String valuesValue) throws JsonProcessingException {

        Parameter paramObj = new Parameter();
        if (StringUtils.isNotEmpty(pid)) {
            paramObj.pid = pid;
        }
        if (StringUtils.isNotEmpty(fromDate)) {
            paramObj.fromDate = fromDate;
        }
        if (StringUtils.isNotEmpty(fromTime)) {
            paramObj.fromTime = fromTime;
        }
        if (StringUtils.isNotEmpty(toDate)) {
            paramObj.toDate = toDate;
        }
        if (StringUtils.isNotEmpty(toTime)) {
            paramObj.toTime = toTime;
        }
        if (StringUtils.isNotEmpty(user)) {
            paramObj.user = user;
        }
        if (StringUtils.isNotEmpty(valuesKey)) {
            paramObj.valuesKey = valuesKey;
        }
        if (StringUtils.isNotEmpty(valuesValue)) {
            paramObj.valuesValue = valuesValue;
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);

        return mapper.writeValueAsString(paramObj);
    }

    private class Parameter {
        public String pid = null;
        public String fromDate = null;
        public String fromTime = null;
        public String toDate = null;
        public String toTime = null;
        public String user = null;
        public String valuesKey = null;
        public String valuesValue = null;
    }

}
