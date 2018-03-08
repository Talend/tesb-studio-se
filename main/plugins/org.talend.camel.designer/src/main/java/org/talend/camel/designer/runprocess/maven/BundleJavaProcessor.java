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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.model.IRepositoryNode;
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
        return super.createMavenPomCreator();
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

        if (option == 1) {

            ProcessItem processItem = (ProcessItem) getProperty().getItem();

            Object bt = processItem.getProperty().getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);

            if (processItem instanceof CamelProcessItem) {
                CamelProcessItem camelProcessItem = (CamelProcessItem) processItem;
                if (bt == null || "ROUTE".equals(bt)) {
                    camelProcessItem.setExportMicroService(false);
                } else {
                    camelProcessItem.setExportMicroService(true);
                }
            }

            try {
                ProcessorUtilities.generateCode(processItem, getContext().getName(), true, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.generatePom(option);
        }
        try {
            IRepositoryNode repositoryNode = RepositorySeekerManager.getInstance().searchRepoViewNode(getProperty().getId(),
                    false);

            IRunnableWithProgress action = new JavaCamelJobScriptsExportWSAction(repositoryNode, getProperty().getVersion(), "",
                    false);
            action.run(new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
