// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.runprocess.maven;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.talend.camel.designer.build.CreateMavenBundlePom;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC sunchaoqun class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class BundleJavaProcessor extends MavenJavaProcessor {

    private static Logger log = Logger.getLogger(BundleJavaProcessor.class);

    @Override
    public void generateEsbFiles() throws ProcessorException {
        super.generateEsbFiles();
    }

    /**
     * DOC sunchaoqun BundleJavaProcessor constructor comment.
     * 
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public BundleJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        this(process, property, filenameFromLabel, true);
    }

    private boolean route;

    public boolean isRoute() {
        return route;
    }

    public BundleJavaProcessor(IProcess process, Property property, boolean filenameFromLabel, boolean isRoute) {
        super(process, property, filenameFromLabel);

        this.route = isRoute;

    }

    @Override
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, int option) throws ProcessorException {
        super.generateCode(statistics, trace, javaProperties, option);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.maven.MavenJavaProcessor#generateCodeAfter(boolean, boolean, boolean, int)
     */
    @Override
    protected void generateCodeAfter(boolean statistics, boolean trace, boolean javaProperties, int option)
            throws ProcessorException {
        if (isStandardJob()) {
            generatePom(option);
        } else {
            try {
                PomUtil.updatePomDependenciesFromProcessor(this);
                new AggregatorPomsHelper().createRoutinesPom(getPomFile(), null);
            } catch (Exception e) {
                throw new ProcessorException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.java.JavaProcessor#isStandardJob()
     */
    @Override
    protected boolean isStandardJob() {
        if (standardJobChanged) {
            return standardJob;
        }

        return super.isStandardJob();
    }

    private boolean standardJob;

    public void setStandardJob(boolean standardJob) {
        this.standardJob = standardJob;
        this.standardJobChanged = true;
    }

    private boolean standardJobChanged;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.maven.MavenJavaProcessor#createMavenPomCreator()
     */
    @Override
    protected IMavenPomCreator createMavenPomCreator() {
        final Property itemProperty = this.getProperty();
        String buildTypeName = null;
        // FIXME, better use the arguments directly for run/export/build/..., and remove this flag later.
        if (ProcessorUtilities.isExportConfig()) {
            // final Object exportType = itemProperty.getAdditionalProperties().get(MavenConstants.NAME_EXPORT_TYPE);
            final Object exportType = getArguments().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            buildTypeName = exportType != null ? exportType.toString() : null;
        } // else { //if run job, will be null (use Standalone by default)

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, itemProperty.getItem());
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, this);
        parameters.put(IBuildPomCreatorParameters.FILE_POM, getPomFile());
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, getAssemblyFile());
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, this.unixClasspath);
        parameters.put(IBuildPomCreatorParameters.CP_WIN, this.windowsClasspath);
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, getArguments());
        parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, Boolean.TRUE);

        AbstractBuildProvider foundBuildProvider = BuildExportManager.getInstance().getBuildProvider(buildTypeName, parameters);
        if (foundBuildProvider != null) {
            final IMavenPomCreator creator = foundBuildProvider.createPomCreator(parameters);
            if (creator != null) {
                return creator;
            }
        }

        // normally, won't be here, should return creator in font.
        CreateMavenBundlePom createTemplatePom = new CreateMavenBundlePom(this, getPomFile());

        createTemplatePom.setUnixClasspath(this.unixClasspath);
        createTemplatePom.setWindowsClasspath(this.windowsClasspath);

        createTemplatePom.setAssemblyFile(getAssemblyFile());

        IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(this.getProperty());
        IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(this.getProperty());
        if (itemLocationPath != null && objectTypeFolder != null) {
            IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
            createTemplatePom.setObjectTypeFolder(objectTypeFolder);
            createTemplatePom.setItemRelativePath(itemRelativePath);
        }

        return createTemplatePom;
    }

    @Override
    protected String getGoals() {
        if (isExportAsOSGI()) {
            // return TalendMavenConstants.GOAL_PACKAGE;
            return TalendMavenConstants.GOAL_COMPILE;
            // return super.getGoals();
        } else {
            return super.getGoals();
        }

    }

    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        // MavenUpdateRequest mavenUpdateRequest = new MavenUpdateRequest(getTalendJavaProject().getProject(), true,
        // false);
        // MavenPlugin.getMavenProjectRegistry().refresh(mavenUpdateRequest);
        super.build(monitor);
    }

    @Override
    protected boolean packagingAndAssembly() {
        return true;
    }

    private static IRepositoryService getRepositoryService() {
        return (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.maven.MavenJavaProcessor#generatePom(int)
     */
    @Override
    public void generatePom(int option) {
        super.generatePom(option);
        try {
            IRepositoryObject repositoryObject = new RepositoryObject(getProperty());

            // Fix TESB-22660: Avoide to operate repo viewer before it open
            if(PlatformUI.isWorkbenchRunning()) {
                 RepositorySeekerManager.getInstance().searchRepoViewNode(getProperty().getId(), false);
            }

            IRunnableWithProgress action = new JavaCamelJobScriptsExportWSAction(repositoryObject, getProperty().getVersion(), "",
                    false);
            action.run(new NullProgressMonitor());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }
}
