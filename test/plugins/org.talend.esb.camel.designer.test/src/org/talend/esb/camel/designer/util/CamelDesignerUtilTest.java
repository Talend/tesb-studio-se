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
package org.talend.esb.camel.designer.util;

import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.designer.util.CamelDesignerUtil;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

public class CamelDesignerUtilTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.util.CamelDesignerUtil#checkRouteInputExistInJob(org.talend.core.model.properties.ProcessItem)}
     * .
     */
    @Test
    public void testCheckRouteInputExistInJob() {
        // check null
        Assert.assertFalse(CamelDesignerUtil.checkRouteInputExistInJob(null));

        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        Assert.assertFalse(CamelDesignerUtil.checkRouteInputExistInJob(item));

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("testRoute");
        property.setVersion("0.1");
        property.setItem(item);
        Process process = new Process(property);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tRouteInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        process.setId("RouteInputJob");
        Node nodeRouteInput = new Node(component, process);
        nodeRouteInput.setLabel("tRouteInput_1");
        process.addNodeContainer(new NodeContainer(nodeRouteInput));

        ProcessType processType = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        processType.setParameters(parameterType);

        NodeType ntRouteInput = TalendFileFactory.eINSTANCE.createNodeType();
        ntRouteInput.setComponentName("tRouteInput");
        ntRouteInput.setPosX(100);
        ntRouteInput.setPosY(100);

        processType.getNode().add(ntRouteInput);
        item.setProperty(property);
        item.setProcess(processType);
        Assert.assertTrue(CamelDesignerUtil.checkRouteInputExistInJob(item));
    }
}
