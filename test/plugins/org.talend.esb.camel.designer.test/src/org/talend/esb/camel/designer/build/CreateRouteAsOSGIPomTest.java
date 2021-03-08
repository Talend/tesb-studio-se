// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.esb.camel.designer.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.designer.build.CreateMavenBundlePom;
import org.talend.camel.designer.runprocess.maven.BundleJavaProcessor;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ProjectManager;

public class CreateRouteAsOSGIPomTest {

    private static final String UNDERSCORE = "_"; //$NON-NLS-1$

    private static final String TEST_ITEM_VERSION = "0.1"; //$NON-NLS-1$

    private static String productVersion;

    private static boolean enable = false;

    @BeforeClass
    public static void initAndCheckProject() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        assertTrue("Test project name changed. Should be 'TEST_NOLOGIN': " + project.getLabel(),
                "TEST_NOLOGIN".equals(project.getLabel()));
        productVersion = VersionUtils.getDisplayVersion();
        VersionUtils.clearCache();
        PomIdsHelper.resetPreferencesManagers();
        System.setProperty(VersionUtils.STUDIO_VERSION_PROP, productVersion + ".UT");
    }

    @AfterClass
    public static void resetToDefault() {
        VersionUtils.clearCache();
        PomIdsHelper.resetPreferencesManagers();
        System.setProperty(VersionUtils.STUDIO_VERSION_PROP, productVersion);
    }

    private IProcessor getProcessor(String name) {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId(name.toLowerCase() + "_Item_ID");
        property.setLabel(name);
        property.setVersion(TEST_ITEM_VERSION);
        ProcessItem item = createProcessItem();

        item.setProperty(property);
        property.setItem(item);

        ProcessType processType = TalendFileFactory.eINSTANCE.createProcessType();
        item.setProcess(processType);

        JobContextManager contextManager = new JobContextManager();
        IContext context = new JobContext("Test");
        contextManager.getListContext().add(context);

        IProcessor processor = createProcessor(property);
        processor.setContext(context);

        return processor;
    }

    private IProcessor createProcessor(Property property) {
        return new BundleJavaProcessor(new RouteProcess(property), property, true, true);
    }

    private ProcessItem createProcessItem() {
        return CamelPropertiesFactory.eINSTANCE.createCamelProcessItem();
    }

    private IMavenPomCreator createPomCreator(IProcessor processor, IFile pomFile) {
        return new CreateMavenBundlePom(processor, pomFile);
    }

    private void compareGeneratedFileWithReference(IProject genProject, String refProjectPath, String filepath)
            throws IOException, CoreException {
        File genFile = genProject.getFile(filepath).getLocation().toFile();

        Bundle b = Platform.getBundle("org.talend.esb.camel.designer.test");
        assertNotNull("Test  bundle cannot be loaded.", b);

        String path = FileLocator.toFileURL(b.getEntry("resources/" + refProjectPath + filepath)).getFile();
        File refFile = Paths.get(path).normalize().toFile();

        assertTrue("Generated '" + genFile + "' file does not exists.", genFile.exists());
        assertTrue("Reference '" + refFile + "' file does not exists.", refFile.exists());

        assertTrue("Generated '" + genFile + "' file is not a file.", genFile.isFile());
        assertTrue("Reference '" + refFile + "' file is not a file.", refFile.isFile());

        String expectedContent = FileUtils.readFileToString(refFile);
        String generatedContent = FileUtils.readFileToString(genFile);
        assertEquals("Content of " + filepath + " are not equals.", expectedContent, generatedContent);
    }

    private void compareGeneratedFilesWithReference(IProject codeProject, String string)
            throws IOException, CoreException {
        if (enable) {
            compareGeneratedFileWithReference(codeProject, string, File.separator + "pom-bundle.xml");
            compareGeneratedFileWithReference(codeProject, string, File.separator + "pom-feature.xml");
            compareGeneratedFileWithReference(codeProject, string, File.separator + TalendMavenConstants.POM_FILE_NAME);
        }
    }

    private void initializeAndCompare(String testCaseName) throws PersistenceException, IOException, CoreException {
        IProcessor processor = getProcessor(testCaseName);
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = ResourceUtils.getProject(project);
        IPath path = getPomPathForTest(testCaseName);
        IMavenPomCreator pomCreator = createPomCreator(processor, fsProject.getFile(path));
        try {
            pomCreator.create(new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.process(e);
            fail(e.getMessage());
        }
        compareGeneratedFilesWithReference(processor.getCodeProject(),
                testCaseName.toLowerCase() + UNDERSCORE + TEST_ITEM_VERSION);
    }

    private IPath getPomPathForTest(String testCaseName) {
        return new Path("poms/jobs/routes" + File.separator + testCaseName.toLowerCase() + UNDERSCORE
                + TEST_ITEM_VERSION + File.separator + TalendMavenConstants.POM_FILE_NAME);
    }

    // ---- TEST CASES BELOW

    @Test
    public void demoRESTRouteAsOSGI() throws PersistenceException, IOException, CoreException {
        String testCaseName = "RouteAsOSGI"; //$NON-NLS-1$
        initializeAndCompare(testCaseName);
    }

}
