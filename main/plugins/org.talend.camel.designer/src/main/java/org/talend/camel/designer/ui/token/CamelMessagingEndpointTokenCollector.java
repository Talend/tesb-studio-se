// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

public class CamelMessagingEndpointTokenCollector extends AbstractTokenCollector {

    private static final String PREF_TOS_JOBS_RECORDS = "esb_cmessagingendpoint_Records"; //$NON-NLS-1$

    private static final String TARGET_COMPONENT = "cMessagingEndpoint";

    @Override
    public void priorCollect() throws Exception {
        JSONObject cMessagingEndpointRecords = new JSONObject();
        IPreferenceStore preferenceStore = CamelDesignerPlugin.getDefault().getPreferenceStore();

        JSONObject ComponentDetails = collectComponentDetails(ERepositoryObjectType.PROCESS_ROUTE,
                ERepositoryObjectType.PROCESS_ROUTELET);
        cMessagingEndpointRecords.put(PROJECTS_REPOSITORY.getKey(), ComponentDetails);// set projects.repository node
        //
        preferenceStore.setValue(PREF_TOS_JOBS_RECORDS, cMessagingEndpointRecords.toString());
    }

    private JSONObject collectComponentDetails(ERepositoryObjectType... types) {
        JSONObject repoStats = new JSONObject();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        try {
            for (ERepositoryObjectType type : types) {
                JSONArray routeArray = new JSONArray();
                JSONObject routeDetailsJson = new JSONObject();//
                routeDetailsJson.put("components", routeArray); // set components node
                JSONObject routeStats = new JSONObject();
                routeStats.put("details", routeDetailsJson); // set details node
                repoStats.put(type.getType(), routeStats); // set the route/routelet node

                Map<String, Integer> camelComponentMap = new HashMap<>();
                Map<String, Integer> customCamelComponentMap = new HashMap<>();
                List<IRepositoryViewObject> allRoutes = factory.getAll(currentProject, type);
                for (IRepositoryViewObject rvo : allRoutes) {
                    Item item = rvo.getProperty().getItem();
                    handleProcessItem(item, camelComponentMap, customCamelComponentMap);
                }

                JSONArray camelComponentsArray = new JSONArray();
                JSONObject camelComponents = null;
                for (Entry<String, Integer> entry : camelComponentMap.entrySet()) {
                    camelComponents = new JSONObject();
                    camelComponents.put(entry.getKey(), entry.getValue());
                    camelComponentsArray.put(camelComponents);
                }

                JSONArray customCamelComponentsArray = new JSONArray();
                JSONObject customCamelComponents = null;
                for (Entry<String, Integer> entry : customCamelComponentMap.entrySet()) {
                    customCamelComponents = new JSONObject();
                    customCamelComponents.put(entry.getKey(), entry.getValue());
                    customCamelComponentsArray.put(customCamelComponents);
                }

                JSONObject CamelJson = new JSONObject();//
                JSONObject customCamelJson = new JSONObject();//
                CamelJson.put("camel.components", camelComponentsArray);
                CamelJson.put("component_name", TARGET_COMPONENT);
                customCamelJson.put("custom.camel.components", customCamelComponentsArray);
                customCamelJson.put("component_name", TARGET_COMPONENT);

                routeArray.put(CamelJson);
                routeArray.put(customCamelJson);
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return repoStats;
    }

    private void handleProcessItem(Item item, Map<String, Integer> camelComponents, Map<String, Integer> customCamelComponents) {
        if (item instanceof ProcessItem) {
            ProcessType processType = ((ProcessItem) item).getProcess();

            for (NodeType node : (List<NodeType>) processType.getNode()) {
                if (TARGET_COMPONENT.equals(node.getComponentName())) {
                    EList elementParameter = node.getElementParameter();
                    for (Object obj : elementParameter) {
                        if (obj instanceof ElementParameterType) {
                            ElementParameterType ep = (ElementParameterType) obj;
                            if (ep.getName().equalsIgnoreCase("HOTLIBS")) {
                                EList elementValue = ep.getElementValue();
                                for (Object ob : elementValue) {
                                    String value = ((ElementValueType) ob).getValue();
                                    recordInMap(camelComponents, value);
                                }
                            } else if (ep.getName().equalsIgnoreCase("LIBRARY")) {
                                String mvnUrl = ep.getValue();
                                mvnUrl = uncloakQuotation(mvnUrl);
                                MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(mvnUrl);
                                String fileName = artifact.getFileName();
                                recordInMap(customCamelComponents, fileName);
                            }
                        }
                    }
                }
            }
        }
    }

    private void recordInMap(Map<String, Integer> components, String component) {
        int num = components.containsKey(component) ? components.get(component) : 0;
        components.put(component, num + 1);
    }

    private String uncloakQuotation(String mvnUrlWithQuotation) {
        int index = 0, lastIndex = mvnUrlWithQuotation.length() - 1;
        for (int i = 0; i < mvnUrlWithQuotation.length(); i++) {
            char charAt = mvnUrlWithQuotation.charAt(i);
            if (charAt != '\"' && charAt != '\'') {
                index = i;
                break;
            }
        }

        for (int i = mvnUrlWithQuotation.length() - 1; i >= 0; i--) {
            char charAt = mvnUrlWithQuotation.charAt(i);
            if (charAt != '\"' && charAt != '\'') {
                lastIndex = i;
                break;
            }
        }

        return mvnUrlWithQuotation.substring(index, lastIndex + 1);
    }

    @Override
    public JSONObject collect() throws Exception {
        IPreferenceStore preferenceStore = CamelDesignerPlugin.getDefault().getPreferenceStore();
        String records = preferenceStore.getString(PREF_TOS_JOBS_RECORDS);
        JSONObject cMessagingEndpointRecords = null;
        try {
            cMessagingEndpointRecords = new JSONObject(records);
        } catch (Exception e) {
            // the value is not set, or is empty
            cMessagingEndpointRecords = new JSONObject();
        }

        return cMessagingEndpointRecords;
    }

}
