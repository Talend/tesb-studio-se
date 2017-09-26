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
package org.talend.esb.designer.esb.runcontainer.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.runprocess.IESBRunContainerService;

/**
 * DOC yyan class global comment. Detailled comment
 */
@SuppressWarnings("restriction")
public class LocalESBRunContainerServiceTest {

    boolean inOSGi = false;

    @Before
    public void setUp() throws Exception {
        inOSGi = ESBRunContainerPlugin.getDefault().getPreferenceStore()
                .getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI);
        ESBRunContainerPlugin.getDefault().getPreferenceStore().setValue(RunContainerPreferenceInitializer.P_ESB_IN_OSGI, true);
        Assert.assertTrue(ESBRunContainerPlugin.getDefault().getPreferenceStore()
                .getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI));
    }

    @After
    public void tearDown() throws Exception {
        ESBRunContainerPlugin.getDefault().getPreferenceStore().setValue(RunContainerPreferenceInitializer.P_ESB_IN_OSGI, inOSGi);
        Assert.assertEquals(
                inOSGi,
                ESBRunContainerPlugin.getDefault().getPreferenceStore()
                        .getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI));
    }

    /**
     * Test method for
     * {@link org.talend.designer.esb.runcontainer.core.LocalESBRunContainerService#createJavaProcessor(org.talend.core.model.process.IProcess, Property, boolean)}
     */
    @Test
    public void testCreateJavaProcessor() {
        IESBRunContainerService service = (IESBRunContainerService) GlobalServiceRegister.getDefault().getService(
                IESBRunContainerService.class);
        String[] esbComponents = { "tESBProviderRequest", "tRESTClient", "tRESTRequest", "tRESTResponse", "tESBConsumer",
                "tESBProviderFault", "tESBProviderRequest", "tESBProviderResponse" };
        for (String esbComponent : esbComponents) {
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            property.setVersion("0.1");
            property.setId("property1");
            property.setLabel("ESBArtifact");
            ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
            processItem.setProperty(property);
            property.setItem(processItem);

            ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
            processItem.setProcess(process);
            NodeType node = TalendFileFactory.eINSTANCE.createNodeType();
            process.getNode().add(node);
            node.setComponentName(esbComponent);
            IComponent tESBComponent = ComponentsFactoryProvider.getInstance().get(esbComponent,
                    ComponentCategory.CATEGORY_4_DI.getName());
            Process esbJob = new Process(property);

            Node tESBComponent_1 = new Node(tESBComponent, esbJob);
            esbJob.addNodeContainer(new NodeContainer(tESBComponent_1));

            Assert.assertNotNull(service.createJavaProcessor(esbJob, property, false));
        }
    }
}
