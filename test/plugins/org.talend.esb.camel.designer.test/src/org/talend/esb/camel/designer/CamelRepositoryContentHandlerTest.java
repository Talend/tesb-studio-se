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
package org.talend.esb.camel.designer;

import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.RouteDocumentItem;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.designer.CamelRepositoryContentHandler;
import org.talend.core.model.repository.ERepositoryObjectType;

public class CamelRepositoryContentHandlerTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.CamelRepositoryContentHandler#createNewItem(org.talend.core.model.repository.ERepositoryObjectType)}
     * .
     */
    @Test
    public void testCreateNewItem() {
        CamelRepositoryContentHandler handler = new CamelRepositoryContentHandler();
        Assert.assertTrue(handler.createNewItem(ERepositoryObjectType.PROCESS_ROUTE) instanceof CamelProcessItem);
        Assert.assertTrue(handler.createNewItem(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "BEANS")) instanceof BeanItem);
        Assert.assertTrue(handler.createNewItem(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "ROUTE_RESOURCES")) instanceof RouteResourceItem);
        Assert.assertTrue(handler.createNewItem(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "ROUTE_DOC")) instanceof RouteDocumentItem);
    }

}
