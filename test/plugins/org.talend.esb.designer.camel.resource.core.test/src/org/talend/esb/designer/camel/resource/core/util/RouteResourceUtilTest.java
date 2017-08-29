// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2013 Talend – www.talend.com
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
package org.talend.esb.designer.camel.resource.core.util;

import static org.junit.Assert.fail;

import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;

public class RouteResourceUtilTest {

    /**
     * Test method for
     * {@link org.talend.designer.camel.resource.core.util.RouteResourceUtil#createDependency(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCreateDependency() {

        // IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL"); //$NON-NLS-1$ //$NON-NLS-2$
        // Process process = new Process(new FakePropertyImpl());
        // Node node = new Node(component, process);
        // ((List<INode>) process.getGraphicalNodes()).add(node);
        //
        // ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        // Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        // String id = ProxyRepositoryFactory.getInstance().getNextId();
        // myProperty.setId(id);
        // ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        // itemState.setDeleted(false);
        // itemState.setPath("");
        // processItem.setState(itemState);
        // processItem.setProperty(myProperty);
        // myProperty.setLabel("myJob");
        // myProperty.setVersion("0.1");
        // processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());

        RouteResourceItem item = CamelPropertiesFactory.eINSTANCE.createRouteResourceItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        String id = ProxyRepositoryFactory.getInstance().getNextId();
        property.setId(id);
        item.setProperty(property);
        String resName = "myResource";
        property.setLabel(resName);
        String resVersion = "0.1";
        property.setVersion(resVersion);
        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        byteArray.setInnerContent(new byte[0]);
        item.setContent(byteArray);
        try {
            ProxyRepositoryFactory.getInstance().create(item, new Path(""));
        } catch (PersistenceException e) {
            e.printStackTrace();
            fail("Test testCreateDependency() method failure.");
        }
        Assert.assertEquals(resName, RouteResourceUtil.createDependency(id, ResourceDependencyModel.LATEST_VERSION).toString());
        Assert.assertEquals(resName, RouteResourceUtil.createDependency(id, resVersion).toString());

        try {
            ProxyRepositoryFactory.getInstance().deleteObjectPhysical(ProxyRepositoryFactory.getInstance().getLastVersion(id));
        } catch (PersistenceException e) {
            e.printStackTrace();
            fail("Test testCreateDependency() method failure, cannot delete myResource.");
        }
    }
}
