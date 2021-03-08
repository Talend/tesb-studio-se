//============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//============================================================================
package org.talend.esb.camel.designer.codegen.config;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.talend.camel.designer.codegen.CamelCodeGenerator;
import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodes.Node;


public class ECamelTemplateTest {

    /**
     * Test method for {@link org.talend.camel.designer.codegen.config.ECamelTemplate#getTemplateURL()}.
     */
    @Test
    public void testGetTemplateURL() {
        ECamelTemplate.CONTEXT.getTemplateURL();
        ECamelTemplate.CAMEL_SPECIALLINKS.getTemplateURL();


        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        String id = ProxyRepositoryFactory.getInstance().getNextId();
        myProperty.setId(id);
        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(false);
        itemState.setPath("");
        processItem.setState(itemState);
        processItem.setProperty(myProperty);
        myProperty.setLabel("myRoute");
        myProperty.setVersion("0.1");
        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());

        IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL"); //$NON-NLS-1$ //$NON-NLS-2$
        RouteProcess process = new RouteProcess(myProperty);
        process.setId("routeprocess");
        INode nodeTimer = new Node(component, process);
        nodeTimer.setLabel("cTimer_1");
        CamelCodeGenerator codeGenerator = new CamelCodeGenerator(process, false, false);
        try {
            String code = codeGenerator.generateProcessCode();
        } catch (CodeGeneratorException e) {
            e.printStackTrace();
            fail("Test testGetTemplateURL() method failure.");
        }
    }

}
