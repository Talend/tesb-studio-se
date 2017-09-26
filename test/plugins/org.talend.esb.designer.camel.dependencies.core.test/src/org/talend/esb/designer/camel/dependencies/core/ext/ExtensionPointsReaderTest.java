// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2017 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.esb.designer.camel.dependencies.core.ext;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.camel.dependencies.core.DependenciesResolver;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

public class ExtensionPointsReaderTest {

    /**
     * Test method for
     * {@link org.talend.designer.camel.dependencies.core.ext.ExtensionPointsReader#getImportPackages(org.talend.designer.core.model.utils.emf.talendfile.NodeType)}
     * .
     */
    @Test
    public void testGetImportPackagesNodeType() {

        String[] imports = new String[] { "javax.xml.namespace", "org.apache.camel", "org.apache.camel.builder",
                "org.apache.camel.component.cxf", "org.apache.camel.component.cxf.common.header", "org.apache.camel.impl",
                "org.apache.camel.main", "org.apache.camel.management", "org.apache.camel.model", "org.apache.camel.spi",
                "org.apache.camel.spring", "org.apache.cxf.endpoint", "org.apache.cxf.headers",
                "org.apache.cxf.interceptor.security", "org.apache.cxf.service.model", "org.apache.cxf.ws.policy",
                "org.apache.cxf.ws.security.wss4j", "org.apache.neethi", "org.springframework.beans.factory.config",
                "org.talend.esb.sam.agent.feature", "org.talend.esb.servicelocator.cxf", "routines.system.api" };

        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("testRoute");
        property.setVersion("0.1");
        property.setItem(item);
        Process process = new Process(property);
        IComponent component = ComponentsFactoryProvider.getInstance().get("cSOAP", ComponentCategory.CATEGORY_4_CAMEL.getName());
        process.setId("soap_route");
        Node nodeRouteInput = new Node(component, process);
        nodeRouteInput.setLabel("cSOAP_1");
        process.addNodeContainer(new NodeContainer(nodeRouteInput));

        ProcessType processType = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        processType.setParameters(parameterType);

        NodeType cSoap = TalendFileFactory.eINSTANCE.createNodeType();
        cSoap.setComponentName("cSOAP");
        cSoap.setPosX(100);
        cSoap.setPosY(100);

        processType.getNode().add(cSoap);
        item.setProperty(property);
        item.setProcess(processType);

        List<String> importPackage = Arrays.asList(new DependenciesResolver(item).getManifestImportPackage(',').split(","));
        Assert.assertTrue(importPackage.indexOf("org.apache.camel.component.cxf") > 0);
        for (String cxfPackage : imports) {
            Assert.assertTrue(importPackage.contains(cxfPackage));
        }
    }

}
