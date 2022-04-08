package jp.ricksoft.AuditLogBrowser.audit.dataextractor;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyDataExtractor extends AbstractDataExtractor {

    private NamespaceService namespaceService;

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    /**
     * @return true if this extractor can do anything with the data
     */
    @Override
    public boolean isSupported(Serializable data) {
        return (data != null && data instanceof Map);
    }

    /**
     * Extract the site name / id
     *
     * @param in a string containing the site id
     * @return the site id
     * @throws Throwable
     */
    @Override
    public Serializable extractData(Serializable in) throws Throwable {

        // The data type of all arguments are passed in Serializable and italready checked using "isSupported" method.
        @SuppressWarnings("unchecked")
        Map<QName, Serializable> path = (Map<QName, Serializable>)in;

        HashMap<String, String> propMap = new HashMap<String, String>();
        path.forEach((key, value) -> {
            propMap.put(convertPrefixedQName(key), convertSerializableToString(value));
        });

        List<String> results = propMap.entrySet().stream()
                    .map(e -> String.join(" : ", e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

        return (Serializable) results;

    }

    private String convertPrefixedQName(QName key) {
        return key.getPrefixedQName(namespaceService).toPrefixString();
    }

    private String convertSerializableToString(Serializable value) {
        if (value instanceof Object) {
            return value.toString();
        } else {
            return String.valueOf(value);
        }
    }

}
