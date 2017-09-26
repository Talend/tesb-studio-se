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
package org.talend.esb.camel.designer.build;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.talend.camel.designer.build.RouteTestCaseBuildProvider;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.ProjectManager;

public class RouteTestCaseBuildProviderTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.build.RouteTestCaseBuildProvider#createPomCreator(java.util.Map)}.
     */
    @Test
    public void testCreatePomCreator() {


        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        String id = ProxyRepositoryFactory.getInstance().getNextId();
        property.setId(id);
        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(false);
        itemState.setPath("");
        processItem.setState(itemState);
        processItem.setProperty(property);
        ProcessType processType = TalendFileFactory.eINSTANCE.createProcessType();
        processItem.setProcess(processType);
        property.setLabel("testRoute");
        property.setVersion("0.1");
        property.setItem(processItem);

        IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL"); //$NON-NLS-1$ //$NON-NLS-2$
        RouteProcess process = new RouteProcess(property);
        process.setId("routeprocess");
        INode nodeTimer = new Node(component, process);
        nodeTimer.setLabel("cTimer_1");
        MavenJavaProcessor processor = new MavenJavaProcessor(process, property, true);

        Project project = ProjectManager.getInstance().getCurrentProject();
        try {
            IProject fsProject = ResourceUtils.getProject(project);
            IPath path = new Path("temp");
            path = path.append("pom.xml");
            IFile file = fsProject.getFile(path);
            String pomContent = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
                    + "  <modelVersion>4.0.0</modelVersion>\n"
                    + "  <artifactId>esb.tooling.test</artifactId>\n"
                    + "  <packaging>jar</packaging>\n" + "  <name>Test POM</name>\n" + "</project>";
            InputStream inputStream = new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8));
            file.delete(true, null);
            file.create(inputStream, false, new NullProgressMonitor());
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(IBuildParametes.ITEM, processItem);
            parameters.put(IBuildPomCreatorParameters.PROCESSOR, processor);
            parameters.put(IBuildPomCreatorParameters.FILE_POM, file);
            parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, Boolean.TRUE);

            IMavenPomCreator creator = new RouteTestCaseBuildProvider().createPomCreator(parameters);
            creator.create(new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test testCreatePomCreator() method failure.");
        }

    }

}
