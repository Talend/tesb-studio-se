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
package org.talend.esb.repository.services.ui.action;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServicesFactory;
import org.talend.repository.services.model.services.impl.ServicesFactoryImpl;
import org.talend.repository.services.ui.action.ExportServiceAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

public class ExportServiceActionTest {

    /**
     * Test method for
     * {@link org.talend.repository.services.ui.action.ExportServiceAction#run(org.eclipse.core.runtime.IProgressMonitor)}
     * .
     */
    @Test
    public void testRun() {

        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        exportChoiceMap.put(ExportChoice.needMavenScript, false);
        ServiceItem serviceItem = ServicesFactory.eINSTANCE.createServiceItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("testSvr");
        serviceItem.setProperty(property);
        ItemState state = PropertiesFactory.eINSTANCE.createItemState();
        serviceItem.setState(state);
        ServiceConnection connection = ServicesFactoryImpl.eINSTANCE.createServiceConnection();
        serviceItem.setConnection(connection);
        try {
            File fileName = File.createTempFile("serviceExportTest", ".jar");

            ExportServiceAction action = new ExportServiceAction(serviceItem, fileName.getAbsolutePath(), exportChoiceMap);
            Assert.assertNotNull(action.getTmpFolderPath());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test testRun() method failure.");
        }
    }

}
