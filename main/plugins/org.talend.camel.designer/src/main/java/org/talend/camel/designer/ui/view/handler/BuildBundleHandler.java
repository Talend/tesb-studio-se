// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.view.handler;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by sunchaoqun on Mar 25, 2016 Detailled comment
 *
 */
public class BuildBundleHandler extends BuildJobHandler {

    /**
     * DOC sunchaoqun BuildMicroServiceHandler constructor comment.
     * 
     * @param processItem
     * @param version
     * @param contextName
     * @param exportChoiceMap
     */
    public BuildBundleHandler(ProcessItem processItem, String version, String contextName,
            Map<ExportChoice, Object> exportChoiceMap) {
        super(processItem, version, contextName, exportChoiceMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.handler.AbstractBuildJobHandler#getProfileArgs()
     */
    @Override
    protected StringBuffer getProfileArgs() {
        StringBuffer profileBuffer = new StringBuffer();

        boolean needMavenScript = exportChoice.get(ExportChoice.needMavenScript) == null
                || isOptionChoosed(ExportChoice.needMavenScript);

        profileBuffer.append(TalendMavenConstants.PREFIX_PROFILE);
        profileBuffer.append(SPACE);

        // should add the default settings always.
        addArg(profileBuffer, true, true, TalendMavenConstants.PROFILE_DEFAULT_SETTING);

        if (needMavenScript) {

            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_JAVA_SOURCES);

            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_MAVEN_RESOURCES);

            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_MICROSERVICE_CONFIGS);

            addArg(profileBuffer, false, TalendMavenConstants.PROFILE_INCLUDE_MICROSERVICE_RUNNING_CONFIGS);

            addArg(profileBuffer, false, TalendMavenConstants.PROFILE_INCLUDE_BINARIES);

        } else {
            addArg(profileBuffer, false, TalendMavenConstants.PROFILE_INCLUDE_JAVA_SOURCES);

            addArg(profileBuffer, false, TalendMavenConstants.PROFILE_INCLUDE_MAVEN_RESOURCES);

            addArg(profileBuffer, false, TalendMavenConstants.PROFILE_INCLUDE_MICROSERVICE_CONFIGS);

            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_MICROSERVICE_RUNNING_CONFIGS);

            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_BINARIES);
        }

        // If the map doesn't contain the assembly key, then take the default value activation from the POM.
        boolean isAssemblyNeeded = exportChoice.get(ExportChoice.needAssembly) == null
                || isOptionChoosed(ExportChoice.needAssembly);
        addArg(profileBuffer, isAssemblyNeeded, TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);

        return profileBuffer;
    }

    private Map<ExportChoice, Object> exportChoiceMap;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler#generateJobFiles(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public IProcessor generateJobFiles(IProgressMonitor monitor) throws Exception {
        // TODO Auto-generated method stub
        return super.generateJobFiles(monitor);
    }

    @Override
    public IFile getJobTargetFile() {
        if (talendProcessJavaProject == null) {
            return null;
        }

        Property jobProperty = processItem.getProperty();
        boolean needLauncher = exportChoice.get(ExportChoice.needLauncher) != null;
        boolean needAssembly = exportChoice.get(ExportChoice.needAssembly) != null;
        String jobName = JavaResourcesHelper.getJobJarName(jobProperty.getLabel(), jobProperty.getVersion())
                + ((needLauncher && needAssembly) ? ".zip" : ".jar");
        IFolder targetFolder = talendProcessJavaProject.getTargetFolder();
        try {
            targetFolder.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        IFile jobFile = targetFolder.getFile(jobName);
        return jobFile;
    }

}
