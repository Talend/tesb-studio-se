// ============================================================================
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.esb.camel.designer.codegen.partgen;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.designer.codegen.partgen.EndpointIdGenerator;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

public class EndpointIdGeneratorTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.codegen.partgen.EndpointIdGenerator#generatePart(org.talend.core.model.process.INode, java.lang.Object[])}
     * .
     */
    @Test
    public void testGeneratePart() {
        IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL"); //$NON-NLS-1$ //$NON-NLS-2$
        Process process = new Process(new FakePropertyImpl());
        process.setId("myRoute");
        INode nodeTimer = new Node(component, process);
        nodeTimer.setLabel("cTimer_1");

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

        try {
            CharSequence generatePart = new EndpointIdGenerator().generatePart(nodeTimer);
            Assert.assertEquals(generatePart.toString(), ".id(\"null_cTimer_1\")");
        } catch (CodeGeneratorException e) {
            e.printStackTrace();
            fail("Test testGeneratePart() method failure.");
        }
    }

}
