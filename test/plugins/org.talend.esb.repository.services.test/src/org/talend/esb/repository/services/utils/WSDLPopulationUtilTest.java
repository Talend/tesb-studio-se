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
package org.talend.esb.repository.services.utils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.repository.services.utils.WSDLPopulationUtil;

public class WSDLPopulationUtilTest {

    /**
     * Test method for {@link org.talend.repository.services.utils.WSDLPopulationUtil#loadWSDL(java.lang.String)}.
     * 
     * To validate an imported XSD/WSDL type files. TESB-19040
     */
    @Test
    public void testLoadWSDL() {
        Bundle b = Platform.getBundle("org.talend.esb.repository.services.test");
        try {
            URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources"), null)); //$NON-NLS-1$
            WSDLPopulationUtil wsdlPopulationUtil = new WSDLPopulationUtil();
            wsdlPopulationUtil.loadWSDL("file://" + url.getPath() + "/client_wsdl/cliente-v1_1.wsdl");
            Assert.assertNotNull(wsdlPopulationUtil
                    .getXSDSchemaFromNamespace("http://www.supervielle.com.ar/xsd/Integracion/common/commonTypes-v1"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Test testGetTemplateURL() method failure.");
        }
    }

}
