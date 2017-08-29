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

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.repository.ProjectManager;
import org.talend.repository.services.utils.WSDLUtils;

public class WSDLUtilsTest {

    /**
     * Test method for
     * {@link org.talend.repository.services.utils.WSDLUtils#getServiceOperationParameters(org.eclipse.core.resources.IFile, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetServiceOperationParameters() {
        Bundle bundle = Platform.getBundle("org.talend.esb.repository.services.test");
        IFile file = null;
        try {
            URL url = FileLocator.toFileURL(FileLocator.find(bundle, new Path("resources/DemoService_0.1.wsdl"), null)); //$NON-NLS-1$

            Project project = ProjectManager.getInstance().getCurrentProject();

            IProject fsProject = ResourceUtils.getProject(project);
            IPath path = new Path("temp");
            String fileName = File.createTempFile("tESBConsumerTest", ".wsdl").getName();
            path = path.append(fileName);
            file = fsProject.getFile(path);
            file.create(url.openStream(), false, new NullProgressMonitor());

            Map<String, String> operations = WSDLUtils.getServiceOperationParameters(file, "DemoServiceOperation",
                    "DemoServicePortType");
            Assert.assertEquals("DemoServicePort", operations.get("PORT_NAME"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test testGetServiceOperationParameters() method failure.");
        } finally {
            if (file != null) {
                try {
                    file.delete(true, null);
                } catch (CoreException e) {
                    e.printStackTrace();
                    fail("Test testGetServiceOperationParameters() delete temp faile failure.");
                }
            }
        }
    }
}
