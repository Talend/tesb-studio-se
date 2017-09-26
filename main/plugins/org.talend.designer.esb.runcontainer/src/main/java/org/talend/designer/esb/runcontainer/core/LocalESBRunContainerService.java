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
package org.talend.designer.esb.runcontainer.core;

import javax.management.MBeanServerConnection;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.process.RunContainerProcessor;
import org.talend.designer.esb.runcontainer.util.JMXUtil;
import org.talend.designer.runprocess.IESBRunContainerService;
import org.talend.designer.runprocess.java.JavaProcessor;
import org.talend.repository.utils.EmfModelUtils;

/**
 * DOC yyan class global comment. Detailled comment <br/>
 * TESB-18750, Locally ESB runtime server service
 */
public class LocalESBRunContainerService implements IESBRunContainerService {

    private boolean enableRuntime;

    @Override
    public void enableRuntime(boolean inRuntime) {
        this.enableRuntime = inRuntime;
    }

    @Override
    public boolean isRuntimeEnable() {
        return ESBRunContainerPlugin.getDefault().getPreferenceStore()
                .getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI);
    }

    /**
     * DOC The local Runtime for ESB will be only taken into account if the user runs an ESB Artifact:
     * 
     * A route (Any Route)
     * 
     * A DataService (SOAP) A DataService (REST)
     * 
     * For tRESTClient or tESBConsumer we use the ESB Runtime
     */
    @Override
    public JavaProcessor createJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        if (ESBRunContainerPlugin.getDefault().getPreferenceStore().getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI)) {
            if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(process.getComponentsType())) {
                if (process.getClass().getName().endsWith("MicroServiceProcess")) {
                    return null;
                } else {
                    return new RunContainerProcessor(process, property, filenameFromLabel);
                }
            } else if (ComponentCategory.CATEGORY_4_DI.getName().equals(process.getComponentsType())) {
                String[] esbComponents = { "tESBProviderRequest", "tRESTClient", "tRESTRequest", "tRESTResponse",
                        "tESBConsumer", "tESBProviderFault", "tESBProviderRequest", "tESBProviderResponse" };
                if (EmfModelUtils.getComponentByName((ProcessItem) property.getItem(), esbComponents) != null) {
                    return new RunContainerProcessor(process, property, filenameFromLabel);
                }
            }
        }
        return null;
    }

    @Override
    public MBeanServerConnection getJMXServerConnection() {
        return JMXUtil.createJMXconnection();
    }
}
