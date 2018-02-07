// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.components.ws.crest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.esb.components.ws.tools.extensions.external.IOASDecoder;
import org.talend.designer.esb.components.ws.tools.extensions.external.RestAPIMapping;

/**
 * DOC dsergent class global comment. Detailled comment
 */
public class CRESTNodeAdapter implements CRESTConstants {

    protected CRESTNode node;

    private IPreferenceStore prefs;

    public CRESTNodeAdapter(CRESTNode node) {
        this.node = node;
        this.prefs = CRESTPlugin.getDefault().getPreferenceStore();
    }

    @SuppressWarnings("unchecked")
    public IStatus setNodeSetting(IOASDecoder oasManager) {

        // endpoint
        boolean keepEndpointValue = prefs.getBoolean(CRESTConstants.PREF_KEEP_ENDPOINT);
        if (StringUtils.isBlank(TalendTextUtils.removeQuotes(node.getParamStringValue(URL))) || !keepEndpointValue) {
            node.setParamValue(URL, TalendTextUtils.addQuotes(oasManager.getEndpoint()));
        }

        node.setParamValue(SERVICE_TYPE, "MANUAL");

        IElementParameter schemasParameter = node.getElementParameter("SCHEMAS");

        List<Map<?, ?>> schemasChildren = (List<Map<?, ?>>) schemasParameter.getValue();

        schemasChildren.clear();

        // API Mappings
        for (RestAPIMapping mapping : oasManager.getMappings()) {

            Map<String, String> newMapping = new LinkedHashMap<>();

            newMapping.put(SCHEMA, mapping.getOutputFlow());
            newMapping.put(HTTP_VERB, mapping.getHttpVerb());
            newMapping.put(URI_PATTERN, TalendTextUtils.addQuotes(mapping.getUriPattern()));
            newMapping.put(CONSUMES, (mapping.getConsumes() != null ? mapping.getConsumes().getLabel() : ""));
            newMapping.put(PRODUCES, (mapping.getProduces() != null ? mapping.getProduces().getLabel() : ""));

            schemasChildren.add(newMapping);

        }

        // documentation
        node.setParamValue(COMMENT, oasManager.getDocumentationComment());

        return Status.OK_STATUS;
    }

    public boolean isEndpointNotNull() {
        return StringUtils.isNotBlank(TalendTextUtils.removeQuotes(node.getParamStringValue(URL)));
    }

    @SuppressWarnings("unchecked")
    public boolean isNodeToDefaultValues() {

        String endpointDefaultValue = "";
        if (node.getElementParameter(URL) != null && node.getElementParameter(URL).getDefaultValues().get(0) != null) {
            endpointDefaultValue = String.valueOf(node.getElementParameter(URL).getDefaultValues().get(0).getDefaultValue());
        }

        IElementParameter schemasParameter = node.getElementParameter("SCHEMAS");
        List<Map<?, ?>> schemasChildren = (List<Map<?, ?>>) schemasParameter.getValue();

        return (endpointDefaultValue.equals(node.getParamStringValue(URL))
                || StringUtils.isBlank(TalendTextUtils.removeQuotes(node.getParamStringValue(URL))))
                && StringUtils.isBlank(node.getParamStringValue(COMMENT)) && schemasChildren.isEmpty();
    }
}
