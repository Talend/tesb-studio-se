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
package org.talend.esb.designer.camel.dependencies.core;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.camel.dependencies.core.DependenciesResolver;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class DependenciesResolverTest {

    public static final String ITEM_ID = "_yVUx8NF4EeG5wOtnVeZxqg";

    /**
     * Test method for
     * {@link org.talend.designer.camel.dependencies.core.DependenciesResolver#DependenciesResolver(org.talend.core.model.properties.ProcessItem)}
     * .
     */
    @Test
    public void testDependenciesResolverProcessItem() {
        String[] importPagkages = new String[] { "org.apache.camel", "org.apache.camel.builder", "org.apache.camel.impl",
                "org.apache.camel.main", "org.apache.camel.management", "org.apache.camel.model", "org.apache.camel.spi",
                "org.apache.camel.spring", "routines.system.api" };
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        processItem.setProperty(property);
        property.setItem(processItem);
        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());
        processItem.getProcess().getNode().add(TalendFileFactory.eINSTANCE.createNodeType());
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentName("test");
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentVersion("0.1");
        property.setId(ITEM_ID);
        property.setVersion("0.1");
        DependenciesResolver resolver = new DependenciesResolver(processItem);
        String[] resolverPackages = resolver.getManifestImportPackage('\n').split("\n");
        List<String> packageList = Arrays.asList(resolverPackages);
        for (String p : importPagkages) {
            Assert.assertTrue(packageList.contains(p));
        }

    }
}
