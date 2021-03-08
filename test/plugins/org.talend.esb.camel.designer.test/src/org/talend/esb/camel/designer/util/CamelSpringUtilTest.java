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
package org.talend.esb.camel.designer.util;

import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.designer.util.CamelSpringUtil;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;

public class CamelSpringUtilTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.util.CamelSpringUtil#getDefaultContent(org.talend.camel.core.model.camelProperties.CamelProcessItem)}
     * .
     */
    @Test
    public void testGetDefaultContent() {
        CamelProcessItem camelProcessItem = CamelPropertiesFactory.eINSTANCE.createCamelProcessItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        camelProcessItem.setProperty(property);
        property.setLabel("testRoute");
        String spring = CamelSpringUtil.getDefaultContent(camelProcessItem);
        Assert.assertNotEquals("", spring);
    }

}
